SECURITY PATTERNS — Cross-Project Security Standards
=====================================================

## Overview
Security measures implemented across the loan.service.ui (Angular) and LoanService (Java) applications.

## Frontend Security (Angular)

### 1. Input Sanitization (appFormField Directive)

**Location:** `loan.service.ui/src/app/shared/directives/form-field.directive.ts`

**Purpose:** Real-time input sanitization against XSS, injection, and unicode abuse attacks.

#### Sanitization Patterns
The directive applies 14 regex patterns on every keystroke:

```typescript
const SANITIZE_PATTERNS: RegExp[] = [
  /[\u2580-\u27BF]/gu,                        // Misc unicode block/drawing symbols
  /[\u{1F300}-\u{1FAFF}]/gu,                  // Emoji ranges
  /top\[/gi,                                   // window.top access attempt
  /\.vibrate\(/gi,                             // Vibration API abuse
  /\\u\p{N}+/gu,                              // Raw unicode escape sequences (\u0041)
  /alert|href/gi,                              // XSS vectors (alert, href)
  /eval\(+/gi,                                 // Code injection (eval)
  /(?:<|%3C)(?=[%!?/a-zA-Z])/gi,             // HTML tag open (<, %3C)
  /style=/gi,                                  // CSS injection (style=)
  /prompt/gi,                                  // Social engineering dialog
  /on\w+=/gi,                                  // Inline event handlers (onclick=, onload=)
  /(?:>|%3E){2,}|(?:<|%3C){2,}/gi,           // Repeated HTML bracket sequences
  /[\w\W]="/gi,                                // Attribute injection (x=")
  /[{|=][a-zA-Z]+:/gi,                         // Template/CSS injection ({color:, =background:)
];
```

#### Features
- **Real-time sanitization:** Strips malicious patterns on input event
- **Cursor preservation:** Maintains cursor position after sanitization
- **Automatic validation:** Adds `invalid` CSS class when control is invalid + touched
- **Touch tracking:** Marks control as touched on blur for error display

#### Usage
Apply to all `<input>` and `<textarea>` elements:
```html
<input appFormField formControlName="customerName" type="text" />
<textarea appFormField formControlName="eventComment" rows="3"></textarea>
```

#### How It Works
```typescript
@HostListener('input', ['$event'])
onInput(event: Event): void {
  const input = event.target as HTMLInputElement;
  const original = input.value;
  const sanitized = this.sanitize(original);
  
  if (sanitized !== original) {
    // Preserve cursor position
    const fromEnd = original.length - (input.selectionEnd ?? original.length);
    input.value = sanitized;
    const newPos = Math.max(0, sanitized.length - fromEnd);
    input.setSelectionRange(newPos, newPos);
    
    // Update reactive form control
    this.ngControl?.control?.setValue(sanitized, { emitEvent: true });
  }
}
```

### 2. Content Security Policy (CSP)

**Add to index.html:**
```html
<meta http-equiv="Content-Security-Policy" 
      content="default-src 'self'; 
               script-src 'self' 'unsafe-inline'; 
               style-src 'self' 'unsafe-inline'; 
               img-src 'self' data:; 
               connect-src 'self' http://localhost:8080;">
```

**Production CSP:**
```html
<meta http-equiv="Content-Security-Policy" 
      content="default-src 'self'; 
               script-src 'self'; 
               style-src 'self'; 
               img-src 'self' data:; 
               connect-src 'self' https://api.production.com;">
```

### 3. Environment-Based Configuration

**Prevent hardcoded URLs:**
```typescript
// ❌ BAD
private readonly apiUrl = 'http://localhost:8080/api';

// ✅ GOOD
import { environment } from 'src/environments/environment';
private readonly apiUrl = environment.apiUrl;
```

### 4. Route Guards (Authentication/Authorization)

**Auth Guard Example:**
```typescript
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    const token = localStorage.getItem('authToken');
    if (token) {
      return true;
    }
    this.router.navigate(['/login']);
    return false;
  }
}
```

**Apply to routes:**
```typescript
{
  path: 'customers',
  loadChildren: () => import('./features/customers/customers.module'),
  canActivate: [AuthGuard]
}
```

### 5. Secure Token Storage

**Use HttpOnly Cookies (Recommended):**
- Store JWT in HttpOnly cookie (set by backend)
- Not accessible via JavaScript
- Prevents XSS token theft

**If Using LocalStorage (Less Secure):**
```typescript
// Store token
localStorage.setItem('authToken', token);

// Retrieve token
const token = localStorage.getItem('authToken');

// Remove token on logout
localStorage.removeItem('authToken');
```

**Best Practice:** Use sessionStorage for temporary sessions:
```typescript
sessionStorage.setItem('authToken', token);
```

### 6. HTTP Interceptor for Authentication

```typescript
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('authToken');
    
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
          'X-Requested-With': 'XMLHttpRequest'  // CSRF protection
        }
      });
    }
    
    return next.handle(req);
  }
}
```

## Backend Security (Java Spring Boot)

### 1. Input Validation

**Service-Level Validation:**
```java
@Override
public void basicValidation(LoanRequest request) {
    // Required field validation
    assertNotBlank("requestedAmount", request.getRequestedAmount());
    
    if (request.getEffectiveDate() == null) {
        throw new IllegalArgumentException("effectiveDate is required");
    }
    
    // Max length validation
    assertMaxLength("requestedAmount", request.getRequestedAmount(), 30);
    assertMaxLength("eventComment", request.getEventComment(), 255);
    
    // Format validation
    if (request.getRequestedAmount() != null) {
        if (!request.getRequestedAmount().matches("^\\d+(\\.\\d{1,2})?$")) {
            throw new IllegalArgumentException("Invalid amount format");
        }
    }
}
```

**Validation Helper Methods:**
```java
protected void assertNotBlank(String fieldName, String value) {
    if (value == null || value.isBlank()) {
        throw new IllegalArgumentException(
            "Field '" + fieldName + "' is required and must not be blank");
    }
}

protected void assertMaxLength(String fieldName, String value, int maxLength) {
    if (value != null && value.length() > maxLength) {
        throw new IllegalArgumentException(
            "Field '" + fieldName + "' exceeds maximum length of " + maxLength);
    }
}
```

### 2. SQL Injection Prevention

**Use JPA/Hibernate (Parameterized Queries):**
```java
// ✅ SAFE — JPA auto-parameterizes
@Query("SELECT lp FROM LoanPrincipalPayment lp WHERE lp.loanId = :loanId")
Optional<LoanPrincipalPayment> findByLoanId(@Param("loanId") String loanId);

// ❌ DANGEROUS — String concatenation
@Query("SELECT lp FROM LoanPrincipalPayment lp WHERE lp.loanId = '" + loanId + "'")
```

**EntityManager with Named Parameters:**
```java
String jpql = "SELECT lp FROM LoanPrincipalPayment lp WHERE lp.loanId = :loanId";
TypedQuery<LoanPrincipalPayment> query = entityManager.createQuery(jpql, LoanPrincipalPayment.class);
query.setParameter("loanId", loanId);
List<LoanPrincipalPayment> results = query.getResultList();
```

### 3. CORS Configuration

**Add WebConfig for CORS:**
```java
package com.loanservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:4200", "https://production-frontend.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### 4. Authentication with Spring Security (If Applicable)

**Add Dependency (build.gradle):**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
```

**Security Configuration:**
```java
package com.loanservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  // Disable CSRF for REST APIs (use token auth)
            .cors()            // Enable CORS
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login", "/api/register").permitAll()
                .requestMatchers("/api/**").authenticated()
            )
            .httpBasic();  // Or JWT filter
        
        return http.build();
    }
}
```

### 5. JWT Token Validation

**JWT Filter (Custom):**
```java
package com.loanservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final String SECRET_KEY = "your-secret-key";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws Exception {
        String header = request.getHeader("Authorization");
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            String username = claims.getSubject();
            
            if (username != null) {
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 6. Error Response Sanitization

**Never expose stack traces in production:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),  // Safe — only validation message
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        // ❌ NEVER do this in production:
        // return ResponseEntity.status(500).body(ex.getStackTrace());
        
        // ✅ Generic message only:
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An error occurred. Please contact support.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### 7. Rate Limiting (Optional)

**Use Bucket4j for rate limiting:**
```gradle
implementation 'com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0'
```

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Bucket bucket = Bucket.builder()
        .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
        .build();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws Exception {
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
        }
    }
}
```

## Database Security

### 1. Parameterized Queries (Always)
Use JPA/Hibernate for auto-parameterization.

### 2. Column-Level Encryption (If Needed)
```java
@Entity
public class LoanPrincipalPayment {
    
    @Convert(converter = SensitiveDataConverter.class)
    @Column(name = "BANK_ACCOUNT_NUMBER")
    private String bankAccountNumber;
}

@Converter
public class SensitiveDataConverter implements AttributeConverter<String, String> {
    private final Cipher cipher;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        // Encrypt
        return encryptData(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        // Decrypt
        return decryptData(dbData);
    }
}
```

### 3. Audit Logging
```java
@Entity
public class LoanPrincipalPayment {
    
    @Column(name = "CREATE_TIMESTAMP", updatable = false)
    private LocalDateTime createTimeStamp;
    
    @Column(name = "CREATED_BY")
    private String createdBy;
    
    @Column(name = "UPDATE_TIMESTAMP")
    private LocalDateTime updateTimeStamp;
    
    @Column(name = "UPDATED_BY")
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createTimeStamp = LocalDateTime.now();
        updateTimeStamp = LocalDateTime.now();
        createdBy = SecurityContextHolder.getContext().getAuthentication().getName();
        updatedBy = createdBy;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTimeStamp = LocalDateTime.now();
        updatedBy = SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
```

## Deployment Security

### 1. Environment Variables
**Never hardcode secrets in code:**

```properties
# ❌ BAD
spring.datasource.password=mySecretPassword

# ✅ GOOD
spring.datasource.password=${DB_PASSWORD}
```

**Set environment variables:**
```bash
export DB_PASSWORD=mySecretPassword
export JWT_SECRET=myJwtSecret
```

### 2. HTTPS Only (Production)
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
server.ssl.key-store-type=PKCS12
```

### 3. Security Headers
Add filter for security headers:
```java
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws Exception {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        
        filterChain.doFilter(request, response);
    }
}
```

## Security Checklist

### Angular Frontend
- [x] Apply `appFormField` directive to all user inputs
- [x] Use environment files for API URLs
- [ ] Implement route guards for protected routes
- [ ] Store tokens in HttpOnly cookies (or secure storage)
- [ ] Add HTTP interceptor for auth headers
- [ ] Enable Content Security Policy
- [ ] Sanitize all dynamic HTML (avoid innerHTML)

### Java Backend
- [x] Validate all inputs in service layer
- [x] Use parameterized queries (JPA/Hibernate)
- [ ] Configure CORS for allowed origins
- [ ] Implement authentication (Spring Security + JWT)
- [ ] Sanitize error messages (no stack traces)
- [ ] Add rate limiting for API endpoints
- [ ] Enable HTTPS in production
- [ ] Use environment variables for secrets
- [ ] Add security headers filter
- [x] Implement audit logging (created_by, updated_by)

### Database
- [x] Use in-memory H2 for development only
- [ ] Switch to production database (PostgreSQL/MySQL)
- [ ] Encrypt sensitive columns
- [ ] Enable database audit logging
- [ ] Restrict database user permissions

### Deployment
- [ ] Use HTTPS only
- [ ] Secrets in environment variables
- [ ] Regular dependency updates
- [ ] Security scanning (OWASP Dependency-Check)
- [ ] Container security (if using Docker)
