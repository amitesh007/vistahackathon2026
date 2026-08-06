JAVA BACKEND ARCHITECTURE — LoanService
========================================

## Technology Stack
- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- H2 Database (in-memory)
- Gradle 8.x
- JUnit 5 + JaCoCo for testing
- Server Port: 8081
- CORS enabled for Angular integration

## Project Structure

```
LoanService/
├── build.gradle                     Gradle build configuration
├── gradle.properties                Gradle properties (SSL workaround)
├── src/main/java/com/loanservice/
│   ├── LoanServiceApplication.java  Spring Boot entry point
│   ├── config/                      Configuration classes
│   │   └── CorsConfig.java          CORS configuration
│   ├── controller/                  REST endpoints
│   │   └── LoanController.java      Single unified controller
│   ├── service/                     Business logic
│   │   ├── BaseIntegrationService.java         Abstract base
│   │   ├── CreateLoanPrincipalPaymentIntegration.java
│   │   ├── UpdateLoanPrincipalPaymentIntegration.java
│   │   ├── GetLoanPrincipalPaymentIntegration.java
│   │   └── DeleteLoanPrincipalPaymentIntegration.java
│   ├── repository/                  Data access
│   │   ├── LoanPrincipalPaymentRepository.java
│   │   ├── LoanPrincipalPaymentRepositoryCustom.java
│   │   └── LoanPrincipalPaymentRepositoryImpl.java
│   ├── entity/                      JPA entities
│   │   └── LoanPrincipalPayment.java
│   ├── model/                       DTOs and request/response models
│   │   ├── LoanRequest.java
│   │   └── YNBooleanSerializer.java
│   └── util/                        Utilities
│       └── TransactionIdGenerator.java
└── src/main/resources/
    └── application.properties       Configuration

src/test/java/com/loanservice/      Unit and integration tests
```

## Application Entry Point

### LoanServiceApplication.java
```java
@SpringBootApplication
public class LoanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoanServiceApplication.class, args);
    }
}
```

## REST Controller Architecture

### Dynamic Service Resolution Pattern
The application uses a **single unified controller** that dynamically resolves service implementations based on the `className` field in the request payload.

### LoanController.java
```java
@RestController
@RequestMapping("/api/loan")
public class LoanController {
    private final ApplicationContext applicationContext;

    public LoanController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody LoanRequest request) {
        BaseIntegrationService service = resolveService(request.getClassName());
        service.basicValidation(request);
        Object result = service.basicExecute(request);
        return ResponseEntity.ok(result);
    }

    private BaseIntegrationService resolveService(String className) {
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("className field required");
        }
        return (BaseIntegrationService) applicationContext.getBean(className);
    }
}
```

### Endpoint Mapping
All four CRUD operations share the same URL but differ by HTTP method:
- **POST** `/api/loan` → Create operation
- **PUT** `/api/loan` → Update operation
- **GET** `/api/loan` → Retrieve operation
- **DELETE** `/api/loan` → Delete operation

### Request Payload Pattern
```json
{
  "className": "CreateLoanPrincipalPaymentIntegration",
  "transaction": "Principal Repayment",
  "requestedAmount": "50000",
  "effectiveDate": "2026-08-15",
  "loanId": "LN123456",
  // ... other fields
}
```

The `className` field determines which service handles the request.

## Service Layer Architecture

### Base Service Pattern
All integration services extend `BaseIntegrationService`:

```java
public abstract class BaseIntegrationService {
    /**
     * Validates required fields and business rules.
     * Throws IllegalArgumentException on validation failure.
     */
    public abstract void basicValidation(LoanRequest request);

    /**
     * Executes the business logic and returns the result.
     */
    public abstract Object basicExecute(LoanRequest request);

    // Validation helpers
    protected void assertNotBlank(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                "Field '" + fieldName + "' is required");
        }
    }

    protected void assertMaxLength(String fieldName, String value, int max) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(
                "Field '" + fieldName + "' exceeds max length " + max);
        }
    }
}
```

### Service Implementation Pattern

#### CreateLoanPrincipalPaymentIntegration.java
```java
@Service("CreateLoanPrincipalPaymentIntegration")
public class CreateLoanPrincipalPaymentIntegration extends BaseIntegrationService {

    @Autowired
    private LoanPrincipalPaymentRepository repository;

    @Override
    public void basicValidation(LoanRequest request) {
        assertNotBlank("requestedAmount", request.getRequestedAmount());
        if (request.getEffectiveDate() == null) {
            throw new IllegalArgumentException("effectiveDate is required");
        }
        assertMaxLength("requestedAmount", request.getRequestedAmount(), 30);
        assertMaxLength("loanAlias", request.getLoanAlias(), 104);
    }

    @Override
    public Object basicExecute(LoanRequest request) {
        LoanPrincipalPayment entity = new LoanPrincipalPayment();
        entity.setLoanTransactionId(TransactionIdGenerator.generate());
        entity.setTransactionType(request.getTransaction());
        entity.setRequestedAmount(request.getRequestedAmount());
        // ... map all fields
        return repository.create(entity);
    }
}
```

**Key Points:**
- Service bean name matches the `className` value
- Validation throws `IllegalArgumentException`
- Execute returns the persisted entity or result object
- Auto-generates transaction IDs for create operations

### Service Naming Convention
Service beans registered with exact class name:
```java
@Service("CreateLoanPrincipalPaymentIntegration")
@Service("UpdateLoanPrincipalPaymentIntegration")
@Service("GetLoanPrincipalPaymentIntegration")
@Service("DeleteLoanPrincipalPaymentIntegration")
```

## Repository Layer Architecture

### Dual Interface Pattern
Combines Spring Data JPA with custom repository:

#### LoanPrincipalPaymentRepository.java (Interface)
```java
@Repository
public interface LoanPrincipalPaymentRepository
    extends JpaRepository<LoanPrincipalPayment, String>,
            LoanPrincipalPaymentRepositoryCustom {
    
    void deleteByLoanTransactionId(String loanTransactionId);
}
```

#### LoanPrincipalPaymentRepositoryCustom.java (Custom Interface)
```java
public interface LoanPrincipalPaymentRepositoryCustom {
    LoanPrincipalPayment create(LoanPrincipalPayment entity);
    LoanPrincipalPayment update(LoanPrincipalPayment entity);
}
```

#### LoanPrincipalPaymentRepositoryImpl.java (Custom Implementation)
```java
@Repository
public class LoanPrincipalPaymentRepositoryImpl
    implements LoanPrincipalPaymentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public LoanPrincipalPayment create(LoanPrincipalPayment entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    @Override
    @Transactional
    public LoanPrincipalPayment update(LoanPrincipalPayment entity) {
        LoanPrincipalPayment merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }
}
```

**Benefits:**
- Inherit Spring Data methods (findAll, findById, etc.)
- Add custom methods with direct EntityManager control
- Explicit transaction management
- Flush for immediate persistence

## Entity Layer (JPA)

### LoanPrincipalPayment.java
```java
@Entity
@Table(name = "LOAN_PRINCIPAL_PAYMENT")
public class LoanPrincipalPayment {

    @Id
    @Column(name = "LOAN_TRANSACTION_ID", length = 24)
    private String loanTransactionId;

    @Column(name = "TRANSACTION_TYPE", length = 50)
    private String transactionType;

    @Column(name = "REQUESTED_AMOUNT", length = 20)
    private String requestedAmount;

    @Column(name = "EFFECTIVE_DATE")
    private LocalDate effectiveDate;

    @Column(name = "PREVENT_ONLINE_DELETION_INDICATOR")
    @JsonSerialize(using = YNBooleanSerializer.class)
    private Boolean preventOnlineDeletionIndicator = Boolean.FALSE;

    @Column(name = "CREATE_TIMESTAMP", updatable = false)
    private LocalDateTime createTimeStamp;

    @Column(name = "UPDATE_TIMESTAMP")
    private LocalDateTime updateTimeStamp;

    @PrePersist
    protected void onCreate() {
        createTimeStamp = LocalDateTime.now();
        updateTimeStamp = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTimeStamp = LocalDateTime.now();
    }

    // Getters and setters...
}
```

**Key Patterns:**
- `@Entity` + `@Table` for table mapping
- String primary key (24-character transaction ID)
- LocalDate/LocalDateTime for temporal fields
- Boolean fields with default FALSE values
- Custom JSON serializer for Y/N boolean fields
- `@PrePersist` / `@PreUpdate` for audit timestamps
- Column length constraints match validation rules

## Model Layer (DTOs)

### LoanRequest.java
```java
public class LoanRequest {
    private String className;              // Service resolver
    private String transaction;            // Transaction type
    private String requestedAmount;
    private LocalDate effectiveDate;
    private String loanTransactionId;     // For update/delete/get
    // ... all entity fields as DTO fields

    // Getters and setters
}
```

**Purpose:**
- Decouples API contract from entity structure
- Includes `className` for service resolution
- Supports all CRUD operations with single DTO

### YNBooleanSerializer.java
```java
public class YNBooleanSerializer extends JsonSerializer<Boolean> {
    @Override
    public void serialize(Boolean value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        gen.writeString((value != null && value) ? "Y" : "N");
    }
}
```

**Purpose:**
- Converts Java Boolean to Y/N string for JSON output
- Applied with `@JsonSerialize(using = YNBooleanSerializer.class)`

## Utility Layer

### TransactionIdGenerator.java
```java
public class TransactionIdGenerator {
    private static final SecureRandom random = new SecureRandom();
    
    public static String generate() {
        long timestamp = System.currentTimeMillis();
        int randomSuffix = 100000 + random.nextInt(900000);
        return String.format("%d%06d", timestamp, randomSuffix);
    }
}
```

**Purpose:**
- Generates unique 24-character transaction IDs
- Combines timestamp + random suffix
- Thread-safe with SecureRandom

## Configuration

### application.properties
```properties
spring.application.name=LoanService
server.port=8081

# H2 in-memory database
spring.datasource.url=jdbc:h2:mem:loandb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console (development only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

**Key Settings:**
- Port 8081 (updated from default 8080)
- In-memory H2 database (no persistence across restarts)
- `create-drop` DDL strategy (recreate schema on startup)
- SQL logging enabled for debugging
- H2 console at http://localhost:8081/h2-console

### CORS Configuration

**Location:** `src/main/java/com/loanservice/config/CorsConfig.java`

```java
package com.loanservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow requests from Angular dev server
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://localhost:4201",
            "http://localhost:4202"
        ));
        
        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers
        config.setAllowedHeaders(Collections.singletonList("*"));
        
        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);
        
        // Max age for preflight requests
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        
        return new CorsFilter(source);
    }
}
```

**Purpose:**
- Enables cross-origin requests from Angular frontend
- Allows requests from multiple Angular dev server ports
- Permits all standard HTTP methods
- Configured for development environment

**Production Notes:**
- Update `allowedOrigins` to production frontend URL
- Remove unnecessary ports (4201, 4202)
- Consider stricter CORS policy

### Gradle Configuration

**gradle.properties** (SSL workaround for corporate environments):
```properties
# Temporary workaround for SSL certificate issues in corporate environments
systemProp.javax.net.ssl.trustStore=NONE
systemProp.javax.net.ssl.trustStoreType=Windows-ROOT
```

**Purpose:**
- Resolves SSL certificate issues in corporate proxy environments
- Uses Windows certificate store for trusted certificates
- Required for Gradle dependency downloads behind corporate firewalls

## Build Configuration (build.gradle)

### Key Dependencies
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### Gradle Tasks
```bash
./gradlew bootJar          # Build executable JAR
./gradlew bootRun          # Run application
./gradlew test             # Run tests
./gradlew jacocoTestReport # Generate code coverage
./gradlew testWithCoverage # Test + coverage report
```

### JaCoCo Code Coverage
- Minimum coverage: 70%
- Reports: build/reports/jacoco/test/html/index.html

### Debug Configuration
```gradle
bootRun {
    jvmArgs = ['-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005']
}
```
Enables remote debugging on port 5005.

## Error Handling Pattern

### Validation Errors
```java
throw new IllegalArgumentException("Field 'requestedAmount' is required");
```
- Controller catches and returns HTTP 400 Bad Request
- Message included in response body

### Service Resolution Errors
```java
throw new IllegalArgumentException("No service found for className: 'XYZ'");
```
- Invalid className returns HTTP 400
- Prevents execution of non-existent services

## API Design Patterns

### Request-Response Flow
1. Client sends POST/PUT/GET/DELETE to `/api/loan`
2. Controller extracts `className` from request body
3. ApplicationContext resolves service bean by name
4. Service validates request (throws exception on error)
5. Service executes business logic
6. Repository persists/retrieves entity
7. Controller returns ResponseEntity.ok(result)

### Why This Pattern?
- **Single endpoint** for all CRUD operations
- **Dynamic dispatch** based on payload
- **Open-closed principle**: Add new services without modifying controller
- **Testable**: Mock ApplicationContext for unit tests

## Best Practices Summary

1. **Controller Layer**
   - Single unified controller per resource
   - Use ApplicationContext for dynamic service resolution
   - Return ResponseEntity with proper HTTP status

2. **Service Layer**
   - Extend BaseIntegrationService
   - Register with exact className as bean name
   - Validate in basicValidation, execute in basicExecute
   - Throw IllegalArgumentException for validation errors

3. **Repository Layer**
   - Extend JpaRepository for standard CRUD
   - Implement custom interface for complex operations
   - Use EntityManager for direct SQL/transaction control

4. **Entity Layer**
   - Map all columns explicitly with @Column
   - Use @PrePersist/@PreUpdate for audit fields
   - Default boolean fields to FALSE
   - Use LocalDate/LocalDateTime for dates

5. **Configuration**
   - Externalize URLs and credentials
   - Use profiles for dev/prod (application-{profile}.properties)
   - Enable H2 console only in development

6. **Testing**
   - Maintain 70%+ code coverage (JaCoCo)
   - Test validation logic separately
   - Use @DataJpaTest for repository tests
   - Use @SpringBootTest for integration tests
