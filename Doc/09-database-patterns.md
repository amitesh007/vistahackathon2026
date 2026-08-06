DATABASE PATTERNS — JPA, Hibernate & H2
========================================

## Overview
Database architecture for the LoanService Spring Boot application using JPA, Hibernate, and H2 in-memory database.

## Database Configuration

### application.properties
```properties
# Application
spring.application.name=LoanService
server.port=8080

# H2 In-Memory Database
spring.datasource.url=jdbc:h2:mem:loandb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console (Development Only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Configuration Explanation

**H2 In-Memory Database:**
- `jdbc:h2:mem:loandb` — In-memory database named "loandb"
- `DB_CLOSE_DELAY=-1` — Keep DB open as long as JVM is running
- `DB_CLOSE_ON_EXIT=FALSE` — Don't close DB when last connection closes

**Hibernate DDL:**
- `create-drop` — Create schema on startup, drop on shutdown
- `create` — Create schema on startup, keep on shutdown
- `update` — Update schema without dropping data
- `validate` — Validate schema matches entities
- `none` — No schema management (production)

**SQL Logging:**
- `show-sql=true` — Log all SQL statements
- `format_sql=true` — Pretty-print SQL for readability

**H2 Console:**
- Access at: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:loandb`
- Username: `sa`, Password: (empty)

## Entity Pattern

### Basic Entity Structure
```java
package com.loanservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.loanservice.model.YNBooleanSerializer;

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

    // Getters and Setters
}
```

### Entity Annotation Guide

**@Entity**
- Marks class as JPA entity
- Hibernate will create table for this class

**@Table(name = "TABLE_NAME")**
- Specifies table name (defaults to class name if omitted)
- Use UPPER_CASE for table names (SQL convention)

**@Id**
- Marks primary key field
- Required for every entity

**@GeneratedValue(strategy = GenerationType.IDENTITY)**
- Auto-generate ID (for numeric IDs)
- Use for auto-increment columns

**@Column Attributes:**
- `name` — Column name in database
- `length` — Max string length (VARCHAR(n))
- `nullable` — Allow NULL values (default true)
- `unique` — Unique constraint
- `updatable` — Allow updates (false for immutable fields)
- `insertable` — Allow inserts (false for computed fields)

**@PrePersist**
- Callback before INSERT operation
- Use for setting created timestamp, default values

**@PreUpdate**
- Callback before UPDATE operation
- Use for updating modified timestamp

### Field Type Mapping

| Java Type | SQL Type (H2) | Notes |
|---|---|---|
| `String` | `VARCHAR(255)` | Default length 255 |
| `Integer` | `INTEGER` | 32-bit integer |
| `Long` | `BIGINT` | 64-bit integer |
| `Double` | `DOUBLE` | Floating-point |
| `Boolean` | `BOOLEAN` | True/False |
| `LocalDate` | `DATE` | Date only (YYYY-MM-DD) |
| `LocalDateTime` | `TIMESTAMP` | Date + Time |
| `BigDecimal` | `DECIMAL` | Precise decimal (for money) |

### Recommended Type for Money
Use `BigDecimal` for precise monetary values:
```java
@Column(name = "REQUESTED_AMOUNT", precision = 19, scale = 2)
private BigDecimal requestedAmount;
```
- `precision` — Total digits
- `scale` — Decimal places

### Boolean Field Pattern
```java
@Column(name = "PREVENT_ONLINE_DELETION_INDICATOR")
@JsonSerialize(using = YNBooleanSerializer.class)
private Boolean preventOnlineDeletionIndicator = Boolean.FALSE;
```
- Store as `BOOLEAN` in database
- Serialize as "Y"/"N" in JSON response
- Default to `Boolean.FALSE` (not null)

## Repository Pattern

### Standard JPA Repository
```java
package com.loanservice.repository;

import com.loanservice.entity.LoanPrincipalPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanPrincipalPaymentRepository extends JpaRepository<LoanPrincipalPayment, String> {
    
    // Inherited methods (no implementation needed):
    // save(entity)
    // findById(id)
    // findAll()
    // deleteById(id)
    // count()
    
    // Custom query methods (Spring Data auto-implements):
    Optional<LoanPrincipalPayment> findByLoanId(String loanId);
    List<LoanPrincipalPayment> findByTransactionType(String transactionType);
    void deleteByLoanTransactionId(String loanTransactionId);
}
```

### Custom Repository Pattern
For complex operations, add custom interface + implementation.

**Step 1: Custom Interface**
```java
public interface LoanPrincipalPaymentRepositoryCustom {
    LoanPrincipalPayment create(LoanPrincipalPayment entity);
    LoanPrincipalPayment update(LoanPrincipalPayment entity);
}
```

**Step 2: Extend in Main Repository**
```java
@Repository
public interface LoanPrincipalPaymentRepository
    extends JpaRepository<LoanPrincipalPayment, String>,
            LoanPrincipalPaymentRepositoryCustom {
    // Combines Spring Data + custom methods
}
```

**Step 3: Implement Custom Methods**
```java
@Repository
public class LoanPrincipalPaymentRepositoryImpl implements LoanPrincipalPaymentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public LoanPrincipalPayment create(LoanPrincipalPayment entity) {
        entityManager.persist(entity);
        entityManager.flush();  // Force immediate insert
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

### Query Methods (Naming Convention)

Spring Data auto-generates queries from method names:

```java
// SELECT * FROM table WHERE fieldName = ?
findByFieldName(String fieldName)

// SELECT * FROM table WHERE field1 = ? AND field2 = ?
findByField1AndField2(String field1, String field2)

// SELECT * FROM table WHERE field1 = ? OR field2 = ?
findByField1OrField2(String field1, String field2)

// SELECT * FROM table WHERE fieldName LIKE ?
findByFieldNameContaining(String fieldName)

// SELECT * FROM table WHERE fieldName LIKE ?%
findByFieldNameStartingWith(String fieldName)

// SELECT * FROM table WHERE fieldName IN (?)
findByFieldNameIn(List<String> values)

// SELECT * FROM table WHERE date BETWEEN ? AND ?
findByDateBetween(LocalDate start, LocalDate end)

// DELETE FROM table WHERE fieldName = ?
deleteByFieldName(String fieldName)

// SELECT COUNT(*) FROM table WHERE fieldName = ?
countByFieldName(String fieldName)
```

### @Query Annotation
For complex queries not expressible via method names:

```java
@Query("SELECT lp FROM LoanPrincipalPayment lp WHERE lp.requestedAmount > :amount")
List<LoanPrincipalPayment> findByAmountGreaterThan(@Param("amount") String amount);

@Query(value = "SELECT * FROM LOAN_PRINCIPAL_PAYMENT WHERE EFFECTIVE_DATE > ?1", nativeQuery = true)
List<LoanPrincipalPayment> findByEffectiveDateAfterNative(LocalDate date);
```

## Transaction Management

### @Transactional Annotation
```java
@Service
public class LoanService {
    
    @Autowired
    private LoanPrincipalPaymentRepository repository;
    
    @Transactional
    public LoanPrincipalPayment createPayment(LoanRequest request) {
        LoanPrincipalPayment entity = new LoanPrincipalPayment();
        // ... set fields
        return repository.save(entity);
    }
    
    @Transactional(readOnly = true)
    public List<LoanPrincipalPayment> getAllPayments() {
        return repository.findAll();
    }
}
```

**Transaction Attributes:**
- `readOnly = true` — Optimize for read-only queries
- `propagation = Propagation.REQUIRED` — Use existing transaction or create new
- `isolation = Isolation.READ_COMMITTED` — Transaction isolation level
- `timeout = 30` — Timeout in seconds
- `rollbackFor = Exception.class` — Rollback on specific exceptions

## EntityManager (Advanced)

### Direct EntityManager Usage
```java
@Repository
public class CustomRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public LoanPrincipalPayment findByIdWithLock(String id) {
        return entityManager.find(
            LoanPrincipalPayment.class, 
            id, 
            LockModeType.PESSIMISTIC_WRITE
        );
    }
    
    public List<LoanPrincipalPayment> executeNativeQuery(String sql) {
        return entityManager.createNativeQuery(sql, LoanPrincipalPayment.class)
            .getResultList();
    }
    
    @Transactional
    public void bulkUpdate(String transactionType, String newDescription) {
        entityManager.createQuery(
            "UPDATE LoanPrincipalPayment lp " +
            "SET lp.transactionDescription = :desc " +
            "WHERE lp.transactionType = :type"
        )
        .setParameter("desc", newDescription)
        .setParameter("type", transactionType)
        .executeUpdate();
    }
}
```

## H2 Database Console

### Accessing H2 Console
1. Start Spring Boot application
2. Navigate to: http://localhost:8081/h2-console
3. Enter connection details:
   - **JDBC URL:** `jdbc:h2:mem:loandb`
   - **User Name:** `sa`
   - **Password:** (leave empty)
4. Click "Connect"

### Useful SQL Queries
```sql
-- View table schema
SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'LOAN_PRINCIPAL_PAYMENT';

-- View all data
SELECT * FROM LOAN_PRINCIPAL_PAYMENT;

-- Count records
SELECT COUNT(*) FROM LOAN_PRINCIPAL_PAYMENT;

-- Filter by date
SELECT * FROM LOAN_PRINCIPAL_PAYMENT WHERE EFFECTIVE_DATE > '2026-01-01';

-- Aggregate queries
SELECT TRANSACTION_TYPE, COUNT(*), SUM(CAST(REQUESTED_AMOUNT AS DECIMAL)) 
FROM LOAN_PRINCIPAL_PAYMENT 
GROUP BY TRANSACTION_TYPE;
```

## Production Database Migration

### Switch to PostgreSQL
**1. Add dependency (build.gradle):**
```gradle
runtimeOnly 'org.postgresql:postgresql'
```

**2. Update application.properties:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/loandb
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate  # Use Flyway/Liquibase for schema
```

### Switch to MySQL
**1. Add dependency (build.gradle):**
```gradle
runtimeOnly 'com.mysql:mysql-connector-j'
```

**2. Update application.properties:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/loandb
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

## Schema Migration (Flyway)

### Add Flyway Dependency
```gradle
implementation 'org.flywaydb:flyway-core'
```

### Migration Scripts
Create: `src/main/resources/db/migration/V1__initial_schema.sql`
```sql
CREATE TABLE LOAN_PRINCIPAL_PAYMENT (
    LOAN_TRANSACTION_ID VARCHAR(24) PRIMARY KEY,
    TRANSACTION_TYPE VARCHAR(50),
    REQUESTED_AMOUNT VARCHAR(20),
    EFFECTIVE_DATE DATE,
    PREVENT_ONLINE_DELETION_INDICATOR BOOLEAN DEFAULT FALSE,
    CREATE_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATE_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_loan_id ON LOAN_PRINCIPAL_PAYMENT(LOAN_ID);
CREATE INDEX idx_effective_date ON LOAN_PRINCIPAL_PAYMENT(EFFECTIVE_DATE);
```

### Configuration
```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.jpa.hibernate.ddl-auto=validate
```

## Best Practices

### Entity Design
1. **Use explicit column names** — `@Column(name = "FIELD_NAME")`
2. **Set default values** — `private Boolean flag = Boolean.FALSE;`
3. **Add audit timestamps** — `@PrePersist`, `@PreUpdate`
4. **Use appropriate types** — `BigDecimal` for money, `LocalDate`/`LocalDateTime` for dates

### Repository Design
1. **Extend JpaRepository** — Get CRUD methods for free
2. **Use query methods** — Spring Data auto-implements from method names
3. **Add custom repository** — For complex queries requiring EntityManager
4. **Parameterize all queries** — Prevent SQL injection

### Transaction Management
1. **@Transactional on service layer** — Not on repository
2. **readOnly = true** — For read-only operations
3. **Explicit rollback rules** — `rollbackFor = Exception.class`
4. **Keep transactions short** — Avoid long-running transactions

### Performance
1. **Index foreign keys** — `@Index(name = "idx_name", columnList = "column_name")`
2. **Lazy loading** — `@ManyToOne(fetch = FetchType.LAZY)`
3. **Batch operations** — Use `saveAll()` instead of multiple `save()`
4. **Query pagination** — `Pageable` parameter in repository methods

### Production Readiness
1. **Use production database** — PostgreSQL, MySQL, not H2
2. **Schema versioning** — Flyway or Liquibase
3. **ddl-auto = validate** — Never use `create-drop` in production
4. **Connection pooling** — HikariCP (default in Spring Boot)
5. **Disable H2 console** — `spring.h2.console.enabled=false`

## Common Issues & Solutions

### Issue: LazyInitializationException
**Cause:** Accessing lazy-loaded field outside transaction.
**Solution:** Use `@Transactional` or eager loading.

### Issue: Detached Entity
**Cause:** Entity modified outside transaction.
**Solution:** Use `merge()` instead of `persist()`.

### Issue: Duplicate Key
**Cause:** Inserting entity with existing ID.
**Solution:** Check if ID exists before insert, or use auto-generation.

### Issue: SQL Syntax Error
**Cause:** Column name mismatch or reserved keyword.
**Solution:** Use `@Column(name = "...")` and avoid SQL keywords.
