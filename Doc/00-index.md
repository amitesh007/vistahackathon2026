KNOWLEDGE BASE INDEX — Quick Reference
======================================

## Document Overview

This knowledge base contains comprehensive documentation for the Vista Hackathon 26 loan service application, including both Angular frontend (loan.service.ui) and Java Spring Boot backend (LoanService).

## Document List

### 01 - Project Overview
**File:** `01-project-overview.txt`  
**Purpose:** High-level project structure, technology stack, features, and design system  
**Topics:**
- Project purpose and structure
- Technology stack (Angular 21, Spring Boot 3.2.5, Java 17)
- Design system (colors, CSS variables)
- Application features (including success confirmation page)
- Backend on port 8081 with CORS enabled

### 02 - Angular Component Standards
**File:** `02-angular-standards.txt`  
**Purpose:** Component architecture, forms, routing, and styling conventions  
**Topics:**
- Component decorator pattern
- Class structure order
- Reactive Forms with FormBuilder
- Routing and lazy loading
- Styling conventions
- Create Customer form example
- FormField directive (security)
- Sidebar navigation

### 03 - Angular Service Standards
**File:** `03-angular-service-standards.md`  
**Purpose:** Service layer patterns, HTTP integration, observables  
**Topics:**
- Injectable pattern
- Class structure for services
- Observable streams with RxJS
- HTTP client integration
- Error handling patterns
- **CommonService pattern** — Shared service with utilities
- **Type safety with models** — TypeScript interfaces
- Date/time utility methods

### 04 - Agent Architecture
**File:** `04-agent-architecture.txt`  
**Purpose:** AI agent system architecture (if applicable)  
**Topics:**
- Agent orchestration patterns
- TypeScript agent system
- Host orchestrator pattern
- GraphRAG knowledge base

### 05 - Angular UI Architecture
**File:** `05-angular-ui-architecture.md`  
**Purpose:** Comprehensive Angular architecture guide  
**Topics:**
- Technology stack (Angular 21, RxJS 7.8, SCSS)
- Project structure and module organization
- **Core module pattern** (services, models, guards)
- NgModule-based architecture (non-standalone)
- Routing with lazy loading
- Component patterns and best practices
- **Dashboard widgets** (pending approvals, charts, calendar)
- Reactive Forms architecture
- Layout system (sidebar, header, breadcrumb)
- Shared module pattern with **SuccessComponent**
- Security with appFormField directive
- Environment configuration
- Build and deployment (Docker, nginx)

### 06 - Java Backend Architecture
**File:** `06-java-backend-architecture.txt`  
**Purpose:** Spring Boot backend architecture  
**Topics:**
- Technology stack (Java 17, Spring Boot 3.2.5, H2 database, port 8081)
- Project structure (controller, service, repository, entity, config)
- **CORS configuration** for Angular frontend integration
- **Gradle properties** for SSL certificate handling
- Dynamic service resolution pattern (className-based routing)
- REST controller with single unified endpoint
- Service layer with BaseIntegrationService
- Repository layer (JPA + custom implementation)
- Entity layer with JPA annotations
- Model/DTO patterns
- Utility classes (TransactionIdGenerator)
- Gradle build configuration
- Error handling patterns

### 07 - API Integration Patterns
**File:** `07-api-integration-patterns.md`  
**Purpose:** Frontend-backend integration  
**Topics:**
- **Nginx proxy configuration** (API gateway, CORS handling)
- Environment configuration (Angular + Spring Boot)
- CORS setup
- HTTP communication patterns
- Angular service layer for API calls
- **CommonService implementation** (shared APIs & utilities)
- Component integration examples
- Request/response payload patterns (Create, Update, Get, Delete)
- Error handling on both sides
- Data type mapping (TypeScript ↔ Java)
- Date and boolean handling
- Loading states
- **Mock data patterns** (development without backend)
- **Change detection strategies** (manual triggering)
- HTTP interceptors for auth
- Testing integration
- Best practices for both frontend and backend

### 08 - Security Patterns
**File:** `08-security-patterns.txt`  
**Purpose:** Security measures across both projects  
**Topics:**
- **Frontend Security:**
  - Input sanitization (appFormField directive with 14 regex patterns)
  - Content Security Policy (CSP)
  - Environment-based configuration
  - Route guards (authentication/authorization)
  - Secure token storage (HttpOnly cookies vs localStorage)
  - HTTP interceptor for authentication
- **Backend Security:**
  - Input validation (service-level)
  - SQL injection prevention (parameterized queries)
  - CORS configuration
  - Spring Security setup
  - JWT token validation
  - Error response sanitization
  - Rate limiting
- **Database Security:**
  - Parameterized queries
  - Column-level encryption
  - Audit logging
- **Deployment Security:**
  - Environment variables for secrets
  - HTTPS configuration
  - Security headers
- Security checklist for both projects

### 09 - Database Patterns
**File:** `09-database-patterns.txt`  
**Purpose:** JPA, Hibernate, and H2 database patterns  
**Topics:**
- Database configuration (H2 in-memory)
- Entity patterns with JPA annotations
- Field type mapping (Java ↔ SQL)
- Boolean and date handling
- Repository patterns (JpaRepository + custom)
- Query method naming conventions
- @Query annotation for complex queries
- Transaction management (@Transactional)
- EntityManager for advanced operations
- H2 Console usage
- Production database migration (PostgreSQL, MySQL)
- Schema migration with Flyway
- Best practices for entity, repository, transaction, performance
- Common issues and solutions

### 10 - Code Generation Guide
**File:** `10-code-generation-guide.txt`  
**Purpose:** Instructions for AI agents generating code  
**Topics:**
- General code quality principles
- **Angular Code Generation:**
  - Component generation checklist (TS, HTML, SCSS, spec, module)
  - Component template with full lifecycle
  - HTML template structure with forms
  - SCSS styles template
  - Service generation template
  - Module generation template
- **Java Code Generation:**
  - Controller generation template
  - Entity generation template
  - Service generation template
- Code generation best practices
- Common mistakes to avoid
- Testing code generation (Angular + Java)
- Summary checklist for completeness

## Quick Reference by Task

### Creating a New Angular Component
1. Read: `05-angular-ui-architecture.txt` (Component Architecture section)
2. Read: `10-code-generation-guide.txt` (Angular Code Generation section)
3. Apply: Component template, HTML template, SCSS styles
4. Ensure: appFormField directive on all inputs

### Creating a New Java Entity
1. Read: `06-java-backend-architecture.txt` (Entity Layer section)
2. Read: `09-database-patterns.txt` (Entity Pattern section)
3. Read: `10-code-generation-guide.txt` (Java Code Generation section)
4. Apply: Entity template with JPA annotations, audit timestamps

### Creating a Service (Angular)
1. Read: `03-angular-service-standards.txt`
2. Read: `05-angular-ui-architecture.txt` (Service Architecture section)
3. Read: `10-code-generation-guide.txt` (Service generation template)
4. Ensure: Injectable with providedIn: 'root', Observable return types

### Creating a Service (Java)
1. Read: `06-java-backend-architecture.txt` (Service Layer section)
2. Read: `10-code-generation-guide.txt` (Service generation template)
3. Ensure: Extends BaseIntegrationService, correct bean name, validation

### Integrating Frontend with Backend
1. Read: `07-api-integration-patterns.txt`
2. Understand: className-based service resolution
3. Apply: Request/response patterns, error handling
4. Test: Use HttpClientTestingModule for Angular tests

### Implementing Forms (Angular)
1. Read: `02-angular-standards.txt` (Forms section)
2. Read: `05-angular-ui-architecture.txt` (Form Architecture section)
3. Apply: ReactiveFormsModule, FormBuilder, Validators
4. Ensure: appFormField directive, isInvalid() helper, markAllAsTouched()

### Securing the Application
1. Read: `08-security-patterns.txt`
2. Apply: Input sanitization (Angular), input validation (Java)
3. Ensure: CORS configured, HTTPS in production, secrets in env variables

### Working with Database
1. Read: `09-database-patterns.txt`
2. Understand: JPA annotations, repository patterns, transactions
3. Apply: Parameterized queries, @Transactional, EntityManager
4. Test: H2 Console (http://localhost:8080/h2-console)

## Key Patterns Summary

### Angular Patterns
- **NgModule-based** (standalone: false on all components)
- **Lazy loading** for all feature modules
- **Reactive Forms** with FormBuilder and Validators
- **Observable streams** for async data (never subscribe in services)
- **appFormField directive** on all user inputs
- **isInvalid() helper** for form field validation
- **takeUntil pattern** for subscription cleanup

### Java Patterns
- **className-based routing** for dynamic service resolution
- **BaseIntegrationService** with basicValidation() and basicExecute()
- **JpaRepository + Custom** for repository layer
- **@PrePersist/@PreUpdate** for audit timestamps
- **String primary keys** (24-char transaction IDs)
- **Y/N serialization** for booleans (YNBooleanSerializer)
- **Parameterized queries** for SQL injection prevention

### Integration Patterns
- **Environment-based URLs** (environment.ts → Spring Boot)
- **ISO 8601 dates** (YYYY-MM-DD, YYYY-MM-DDTHH:mm:ss)
- **className field** in request payload to resolve service
- **Consistent field naming** (camelCase in both TypeScript and Java)
- **Error handling** on both frontend and backend
- **CORS configuration** for cross-origin requests

## Design System Reference

### Colors
```scss
--primary-color: #694ED6    // Purple
--secondary-color: #C137A2  // Pink
--success-color: #00C9A7
--warning-color: #FFB946
--danger-color: #FF6B6B
--info-color: #4E73DF
```

### Spacing
- Sidebar width: 240px (expanded), 70px (collapsed)
- Header height: 70px
- Form padding: 24px
- Form gap: 20px

### Border Radius
```scss
--radius-sm: 4px
--radius-md: 8px
--radius-lg: 12px
--radius-xl: 16px
```

### Shadows
```scss
--shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.08)
--shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1)
--shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1)
```

## File Structure Reference

### Angular (loan.service.ui)
```
src/app/
├── app.module.ts
├── app-routing.module.ts
├── app.component.ts
├── features/              # Lazy-loaded feature modules
│   ├── dashboard/
│   ├── customers/
│   ├── roles/
│   └── templates/
├── layout/                # Application shell
│   ├── header/
│   ├── sidebar/
│   └── breadcrumb/
└── shared/                # Reusable utilities
    ├── shared.module.ts
    └── directives/
        └── form-field.directive.ts
```

### Java (LoanService)
```
src/main/java/com/loanservice/
├── LoanServiceApplication.java
├── controller/            # REST endpoints
├── service/              # Business logic
├── repository/           # Data access
├── entity/               # JPA entities
├── model/                # DTOs
└── util/                 # Utilities
```

## Common Commands

### Angular
```bash
npm start                    # Start dev server (localhost:4200)
npm run build:prod           # Production build
npm test                     # Run unit tests
ng generate component name   # Generate component
ng generate service name     # Generate service
```

### Java
```bash
./gradlew bootJar           # Build executable JAR
./gradlew bootRun           # Run application
./gradlew test              # Run tests
./gradlew jacocoTestReport  # Generate coverage report
./gradlew testWithCoverage  # Test + coverage in one command
```

### Docker
```bash
docker build -t loan-service-ui .           # Build Angular image
docker run -p 80:80 loan-service-ui         # Run Angular container
docker build -t loan-service-backend .      # Build Java image
docker run -p 8080:8080 loan-service-backend # Run Java container
```

## Environment URLs

### Development
- **Angular:** http://localhost:4200
- **Spring Boot API:** http://localhost:8080/api
- **H2 Console:** http://localhost:8080/h2-console

### API Endpoints (Example)
- `POST /api/loan` — Create loan payment
- `PUT /api/loan` — Update loan payment
- `GET /api/loan` — Get loan payment
- `DELETE /api/loan` — Delete loan payment

## Getting Help

### By Topic
- **Angular components:** Read 02, 05, 10
- **Angular services:** Read 03, 05, 10
- **Java backend:** Read 06, 10
- **API integration:** Read 07
- **Security:** Read 08
- **Database:** Read 09
- **Code generation:** Read 10

### By Error
- **Form validation errors:** Read 02 (Forms section), 05 (Form Architecture)
- **API call errors:** Read 07 (Error Handling section)
- **Database errors:** Read 09 (Common Issues section)
- **Security concerns:** Read 08 (full document)

### By Task
- **New screen:** Read 02, 05, 10 (Angular sections)
- **New API endpoint:** Read 06, 10 (Java sections)
- **New database table:** Read 09 (Entity Pattern section)
- **Form with validation:** Read 02, 05 (Forms sections)
- **Service integration:** Read 03, 07 (Service + Integration)

## Version Information

- **Angular:** 21.0.0
- **TypeScript:** 5.9.2
- **RxJS:** 7.8.0
- **Java:** 17
- **Spring Boot:** 3.2.5
- **Gradle:** 8.x
- **H2 Database:** In-memory

## Last Updated
2026-08-06

---

**Note:** This knowledge base is designed for AI agents generating code for the Vista Hackathon 26 project. Follow the patterns and templates in these documents to ensure consistency, quality, and best practices across all generated code.
