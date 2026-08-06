API INTEGRATION PATTERNS — Frontend ↔ Backend
=============================================

## Overview
This document describes how the Angular frontend (loan.service.ui) integrates with the Java Spring Boot backend (LoanService).

## Nginx Proxy Configuration

### Purpose
The nginx proxy configuration provides:
1. **API Gateway**: Single entry point for all API calls
2. **CORS Handling**: Centralized CORS headers
3. **Development/Production Flexibility**: Easy environment switching
4. **Security**: Additional layer for authentication/authorization

### Configuration File
**Location:** `loan.service.ui/nginx.conf`

```nginx
# API proxy - forwards /api/* to backend service
location /api/ {
    # For local development, use localhost:8081 (Spring Boot configured port)
    # For production/Docker, use backend-service:8081
    proxy_pass http://backend-service:8081/api/;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection 'upgrade';
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_cache_bypass $http_upgrade;
    
    # CORS headers (if needed)
    add_header 'Access-Control-Allow-Origin' '*' always;
    add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
    add_header 'Access-Control-Allow-Headers' 'Authorization, Content-Type, Accept' always;
    
    # Handle preflight requests
    if ($request_method = 'OPTIONS') {
        return 204;
    }
}
```

### API Flow
```
Angular Component → CommonService → /api/approvals → Nginx Proxy → Spring Boot :8081/api/approvals
```

## Environment Configuration

### Angular Environment Files
Located: `src/environments/`

**environment.ts** (Development)
```typescript
export const environment = {
  production: false,
  apiUrl: '/api',  // Proxied by nginx - no need for full URL
  appName: 'Finastra Loan Service',
  version: '1.0.0'
};
```

**environment.prod.ts** (Production)
```typescript
export const environment = {
  production: true,
  apiUrl: '/api',  // Still proxied by nginx in production
  appName: 'Finastra Loan Service',
  version: '1.0.0'
};
```

### Spring Boot Configuration
Located: `src/main/resources/application.properties`

```properties
server.port=8081
spring.application.name=LoanService

# CORS configuration handled by CorsConfig.java
# See: src/main/java/com/loanservice/config/CorsConfig.java
```

### CORS Setup

**Implementation:** The application uses a dedicated CORS configuration class.

**Location:** `src/main/java/com/loanservice/config/CorsConfig.java`

```java
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
        
        // Allow credentials
        config.setAllowCredentials(true);
        
        // Max age for preflight requests
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        
        return new CorsFilter(source);
    }
}
```

**Features:**
- Allows requests from multiple Angular dev server ports (4200, 4201, 4202)
- Permits all standard HTTP methods
- Configured via CorsFilter bean
- Applies to all /api/** endpoints

## HTTP Communication Pattern

### Angular Service Layer
Create dedicated service for each backend resource.

#### Example: LoanService (Angular)
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

export interface LoanRequest {
  className: string;
  transaction: string;
  requestedAmount: string;
  effectiveDate: string;
  loanId?: string;
  loanTransactionId?: string;
  // ... other fields
}

export interface LoanResponse {
  loanTransactionId: string;
  transactionType: string;
  requestedAmount: string;
  effectiveDate: string;
  createTimeStamp: string;
  // ... other fields
}

@Injectable({ providedIn: 'root' })
export class LoanService {
  private readonly baseUrl = `${environment.apiUrl}/loan`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new loan principal payment
   */
  createLoanPayment(request: LoanRequest): Observable<LoanResponse> {
    const payload = {
      ...request,
      className: 'CreateLoanPrincipalPaymentIntegration'
    };
    return this.http.post<LoanResponse>(this.baseUrl, payload)
      .pipe(
        tap(response => console.log('Created:', response)),
        catchError(this.handleError)
      );
  }

  /**
   * Update an existing loan principal payment
   */
  updateLoanPayment(request: LoanRequest): Observable<LoanResponse> {
    const payload = {
      ...request,
      className: 'UpdateLoanPrincipalPaymentIntegration'
    };
    return this.http.put<LoanResponse>(this.baseUrl, payload)
      .pipe(
        tap(response => console.log('Updated:', response)),
        catchError(this.handleError)
      );
  }

  /**
   * Get a loan principal payment by transaction ID
   */
  getLoanPayment(loanTransactionId: string): Observable<LoanResponse> {
    const payload = {
      className: 'GetLoanPrincipalPaymentIntegration',
      loanTransactionId
    };
    return this.http.request<LoanResponse>('GET', this.baseUrl, { body: payload })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Delete a loan principal payment
   */
  deleteLoanPayment(loanTransactionId: string): Observable<void> {
    const payload = {
      className: 'DeleteLoanPrincipalPaymentIntegration',
      loanTransactionId
    };
    return this.http.request<void>('DELETE', this.baseUrl, { body: payload })
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      }
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
```

### Component Integration
Use the service in components via dependency injection.

#### Example: Create Loan Payment Component
```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LoanService, LoanRequest } from '../../services/loan.service';

@Component({
  selector: 'app-create-loan-payment',
  standalone: false,
  templateUrl: './create-loan-payment.component.html',
  styleUrls: ['./create-loan-payment.component.scss']
})
export class CreateLoanPaymentComponent {
  loanForm: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private loanService: LoanService,
    private router: Router
  ) {
    this.loanForm = this.fb.group({
      transaction: ['Principal Repayment', Validators.required],
      requestedAmount: ['', [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]],
      effectiveDate: ['', Validators.required],
      loanId: [''],
      eventComment: ['', Validators.maxLength(255)]
      // ... other fields
    });
  }

  onSubmit(): void {
    this.loanForm.markAllAsTouched();
    
    if (this.loanForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const request: LoanRequest = {
        className: 'CreateLoanPrincipalPaymentIntegration',
        ...this.loanForm.value
      };

      this.loanService.createLoanPayment(request).subscribe({
        next: (response) => {
          console.log('Loan payment created:', response);
          this.router.navigate(['/loans']);
        },
        error: (err) => {
          this.errorMessage = err.message || 'Failed to create loan payment';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/loans']);
  }

  isInvalid(controlName: string): boolean {
    const ctrl = this.loanForm.get(controlName);
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }
}
```

## Request/Response Patterns

### Create Request (POST /api/loan)
**Angular Payload:**
```typescript
{
  className: "CreateLoanPrincipalPaymentIntegration",
  transaction: "Principal Repayment",
  requestedAmount: "50000",
  effectiveDate: "2026-08-15",
  loanId: "LN123456",
  eventComment: "Regular payment",
  preventOnlineDeletionIndicator: false
}
```

**Spring Boot Response:**
```json
{
  "loanTransactionId": "17229134567890123456",
  "transactionType": "Principal Repayment",
  "requestedAmount": "50000",
  "effectiveDate": "2026-08-15",
  "loanId": "LN123456",
  "eventComment": "Regular payment",
  "preventOnlineDeletionIndicator": "N",
  "createTimeStamp": "2026-08-06T10:30:00",
  "updateTimeStamp": "2026-08-06T10:30:00"
}
```

### Update Request (PUT /api/loan)
**Angular Payload:**
```typescript
{
  className: "UpdateLoanPrincipalPaymentIntegration",
  loanTransactionId: "17229134567890123456",
  requestedAmount: "55000",
  eventComment: "Updated amount"
}
```

**Spring Boot Response:**
```json
{
  "loanTransactionId": "17229134567890123456",
  "transactionType": "Principal Repayment",
  "requestedAmount": "55000",
  "eventComment": "Updated amount",
  "updateTimeStamp": "2026-08-06T11:00:00"
}
```

### Get Request (GET /api/loan)
**Angular Payload:**
```typescript
{
  className: "GetLoanPrincipalPaymentIntegration",
  loanTransactionId: "17229134567890123456"
}
```

**Spring Boot Response:**
```json
{
  "loanTransactionId": "17229134567890123456",
  "transactionType": "Principal Repayment",
  "requestedAmount": "55000",
  "effectiveDate": "2026-08-15"
}
```

### Delete Request (DELETE /api/loan)
**Angular Payload:**
```typescript
{
  className: "DeleteLoanPrincipalPaymentIntegration",
  loanTransactionId: "17229134567890123456"
}
```

**Spring Boot Response:**
```json
{}
```
(Empty object on successful deletion)

## Error Handling

### Backend Error Format
Spring Boot returns errors in this format:
```json
{
  "timestamp": "2026-08-06T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Field 'requestedAmount' is required",
  "path": "/api/loan"
}
```

### Angular Error Handler
```typescript
private handleError(error: HttpErrorResponse): Observable<never> {
  let errorMessage = 'An error occurred';
  
  if (error.status === 0) {
    // Network error
    errorMessage = 'Network error. Please check your connection.';
  } else if (error.status === 400) {
    // Validation error
    errorMessage = error.error?.message || 'Invalid request';
  } else if (error.status === 404) {
    errorMessage = 'Resource not found';
  } else if (error.status === 500) {
    errorMessage = 'Server error. Please try again later.';
  }
  
  return throwError(() => new Error(errorMessage));
}
```

### Display Errors in Template
```html
<form [formGroup]="loanForm" (ngSubmit)="onSubmit()">
  <!-- Form fields -->
  
  <div *ngIf="errorMessage" class="error-banner">
    {{ errorMessage }}
  </div>
  
  <div class="form-actions">
    <button type="submit" [disabled]="isLoading">
      <span *ngIf="isLoading">Saving...</span>
      <span *ngIf="!isLoading">Submit</span>
    </button>
    <button type="button" (click)="onCancel()">Cancel</button>
  </div>
</form>
```

## Data Type Mapping

### Angular TypeScript ↔ Java Type Mapping
| Angular Type | Java Type | Notes |
|---|---|---|
| `string` | `String` | Direct mapping |
| `number` | `Integer`, `Long`, `Double` | Parse on backend |
| `boolean` | `Boolean` | Y/N serialization on backend |
| `Date` | `LocalDate` | ISO 8601 format (YYYY-MM-DD) |
| `Date` (with time) | `LocalDateTime` | ISO 8601 (YYYY-MM-DDTHH:mm:ss) |

### Date Handling
**Angular:**
```typescript
effectiveDate: '2026-08-15'  // ISO 8601 string
```

**Java:**
```java
@Column(name = "EFFECTIVE_DATE")
private LocalDate effectiveDate;  // Jackson auto-converts
```

### Boolean Handling
**Angular sends:**
```typescript
preventOnlineDeletionIndicator: true
```

**Java receives:**
```java
private Boolean preventOnlineDeletionIndicator;
```

**Java responds:**
```json
{
  "preventOnlineDeletionIndicator": "Y"
}
```
(via `YNBooleanSerializer`)

## Loading States

### Service-Level Loading State
```typescript
@Injectable({ providedIn: 'root' })
export class LoanService {
  private readonly _isLoading = new BehaviorSubject<boolean>(false);
  public readonly isLoading$ = this._isLoading.asObservable();

  createLoanPayment(request: LoanRequest): Observable<LoanResponse> {
    this._isLoading.next(true);
    return this.http.post<LoanResponse>(this.baseUrl, request).pipe(
      tap(() => this._isLoading.next(false)),
      catchError((err) => {
        this._isLoading.next(false);
        return throwError(() => err);
      })
    );
  }
}
```

### Component-Level Loading State
```typescript
export class CreateLoanPaymentComponent {
  isLoading = false;

  onSubmit(): void {
    this.isLoading = true;
    this.loanService.createLoanPayment(request).subscribe({
      next: () => this.isLoading = false,
      error: () => this.isLoading = false
    });
  }
}
```

## HTTP Interceptor Pattern

### Add Authorization Header (If Needed)
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
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    return next.handle(req);
  }
}
```

**Register in AppModule:**
```typescript
import { HTTP_INTERCEPTORS } from '@angular/common/http';

providers: [
  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
]
```

## Testing Integration

### Angular Service Testing
```typescript
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LoanService } from './loan.service';

describe('LoanService', () => {
  let service: LoanService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LoanService]
    });
    service = TestBed.inject(LoanService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should create loan payment', () => {
    const mockResponse = { loanTransactionId: '123', requestedAmount: '50000' };
    const request = { className: 'CreateLoanPrincipalPaymentIntegration', requestedAmount: '50000' };

    service.createLoanPayment(request).subscribe(response => {
      expect(response.loanTransactionId).toBe('123');
    });

    const req = httpMock.expectOne('http://localhost:8081/api/loan');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  afterEach(() => {
    httpMock.verify();
  });
});
```

## Best Practices

### Angular Frontend
1. **Centralize API URLs** — Use environment files
2. **Type all requests/responses** — Define interfaces
3. **Handle errors consistently** — Use catchError in services
4. **Show loading states** — Improve UX
5. **Never subscribe in services** — Return observables
6. **Unsubscribe on destroy** — Prevent memory leaks (use async pipe or takeUntil)
7. **Validate before submit** — Mark form as touched to show errors

### Java Backend
1. **Consistent response format** — Use DTOs
2. **Validate all inputs** — Throw IllegalArgumentException
3. **Return proper HTTP codes** — 200 OK, 400 Bad Request, 500 Internal Server Error
4. **Enable CORS** — For Angular dev server
5. **Log all requests** — Use Spring Boot logging
6. **Use DTOs for API** — Don't expose entities directly
7. **Document APIs** — Use Swagger/OpenAPI (if applicable)

### Integration
1. **Use className pattern** — For dynamic service resolution
2. **ISO 8601 dates** — Standard format for interoperability
3. **Consistent field naming** — camelCase in Angular, snake_case in DB
4. **Version your APIs** — `/api/v1/loan` for future compatibility
5. **Test integration end-to-end** — Use Postman or integration tests

## CommonService Pattern

### Purpose
The `CommonService` is a shared service providing:
1. Common business APIs (approvals, workflows)
2. Utility methods (date/time formatting)
3. Mock data for development

### Implementation Example
**Location:** `src/app/core/services/common.service.ts`

```typescript
@Injectable({
  providedIn: 'root'
})
export class CommonService {
  private apiUrl = '/api/approvals'; // Proxied by nginx

  constructor(private http: HttpClient) {}

  // API Methods
  getPendingApprovals(page: number, pageSize: number, sortBy: string): Observable<ApprovalListResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('pageSize', pageSize.toString())
      .set('sortBy', sortBy);
    return this.http.get<ApprovalListResponse>(this.apiUrl, { params });
  }

  approveRequest(approvalId: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${approvalId}/approve`, {});
  }

  rejectRequest(approvalId: string, reason?: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${approvalId}/reject`, { reason });
  }

  // Utility Methods
  getCurrentDateTime(): Date {
    return new Date();
  }

  getDateDaysAgo(days: number): Date {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date;
  }

  formatDate(date: Date, format: 'short' | 'long' = 'long'): string {
    if (format === 'short') {
      return date.toLocaleDateString();
    }
    return date.toLocaleDateString('en-US', { 
      weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' 
    });
  }

  formatTime(date: Date, includeSeconds: boolean = true): string {
    return date.toLocaleTimeString('en-US', {
      hour: 'numeric', minute: '2-digit',
      second: includeSeconds ? '2-digit' : undefined,
      hour12: true
    });
  }
}
```

## Mock Data for Development

### Purpose
Mock data allows frontend development without backend dependency.

### Pattern
```typescript
private getMockApprovals(page: number, pageSize: number, sortBy: string): Observable<ApprovalListResponse> {
  // Use current date/time for realistic mock data
  const mockData: PendingApproval[] = [
    {
      id: '1',
      customer: {
        name: 'Lorem Ipsum',
        initials: 'LI'
      },
      products: ['FCC', 'Trade Innovation'],
      task: 'Creation',
      createdDate: this.getDateDaysAgo(1), // Yesterday
      status: 'pending'
    },
    // ... more mock data
  ];

  // Simulate sorting
  const sortedData = [...mockData].sort((a, b) => {
    if (sortBy === 'customer') {
      return a.customer.name.localeCompare(b.customer.name);
    }
    // ... other sorting logic
    return 0;
  });

  // Simulate pagination
  const startIndex = (page - 1) * pageSize;
  const paginatedData = sortedData.slice(startIndex, startIndex + pageSize);

  const response: ApprovalListResponse = {
    approvals: paginatedData,
    total: mockData.length,
    page,
    pageSize
  };

  // Simulate network delay
  return of(response).pipe(delay(500));
}
```

### Switching to Real API
```typescript
getPendingApprovals(page: number, pageSize: number, sortBy: string): Observable<ApprovalListResponse> {
  // For development: use mock data
  return this.getMockApprovals(page, pageSize, sortBy);
  
  // For production: uncomment real API call
  // const params = new HttpParams()
  //   .set('page', page.toString())
  //   .set('pageSize', pageSize.toString())
  //   .set('sortBy', sortBy);
  // return this.http.get<ApprovalListResponse>(this.apiUrl, { params });
}
```

## Change Detection Patterns

### Manual Change Detection
Some scenarios require manual change detection triggering:

```typescript
import { Component, ChangeDetectorRef } from '@angular/core';

export class PendingApprovalsComponent {
  loading = false;
  approvals: PendingApproval[] = [];

  constructor(
    private commonService: CommonService,
    private cdr: ChangeDetectorRef  // Inject ChangeDetectorRef
  ) {}

  loadApprovals(): void {
    this.loading = true;
    
    this.commonService.getPendingApprovals(1, 10, this.sortBy)
      .subscribe({
        next: (response) => {
          this.approvals = response.approvals;
          this.loading = false;
          this.cdr.detectChanges(); // Manually trigger change detection
        },
        error: (err) => {
          this.loading = false;
          this.cdr.detectChanges(); // Ensure UI updates on error too
        }
      });
  }
}
```

### When to Use Manual Change Detection
1. **Async operations** that update multiple state properties
2. **Template not updating** after observable subscription
3. **Third-party library integration** that operates outside Angular's zone
4. **Performance optimization** to control when change detection runs

### Template Pattern with Loading/Error States
```html
<div class="widget">
  <!-- Loading State -->
  <div *ngIf="loading" class="loading-state">
    <div class="spinner"></div>
    <p>Loading approvals...</p>
  </div>

  <!-- Error State -->
  <div *ngIf="error && !loading" class="error-state">
    <p class="error-message">{{ error }}</p>
    <button (click)="loadApprovals()">Retry</button>
  </div>

  <!-- Data Display -->
  <div *ngIf="!loading && !error">
    <table *ngIf="approvals.length > 0">
      <!-- table content -->
    </table>
    <div *ngIf="approvals.length === 0" class="empty-state">
      <p>No pending approvals</p>
    </div>
  </div>
</div>
```
