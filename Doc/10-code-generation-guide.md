CODE GENERATION GUIDE — Agent Instructions
===========================================

## Overview
This document provides comprehensive guidelines for AI agents generating code for the loan.service.ui (Angular) and LoanService (Java Spring Boot) projects.

## General Principles

### Code Quality Standards
1. **Follow existing patterns** — Study existing code before generating new code
2. **Consistency** — Match naming conventions, structure, and style
3. **Completeness** — Generate all required files (component, template, styles, module, routing, tests)
4. **No placeholders** — Never use `// ... rest of the code` or `// TODO: implement`
5. **Type safety** — Always use TypeScript interfaces, Java generics
6. **Documentation** — Add JavaDoc/TSDoc comments for public APIs
7. **Error handling** — Include try-catch, error messages, fallback logic

### File Organization
- One feature = One module (Angular)
- Separate concerns: controller → service → repository (Java)
- Co-locate related files (component + template + styles)
- Use clear, descriptive filenames

## Angular Code Generation

### Component Generation Checklist
When generating an Angular component, create ALL of these files:

```
feature-name/
├── feature-name.component.ts        # TypeScript class
├── feature-name.component.html      # Template
├── feature-name.component.scss      # Styles
├── feature-name.component.spec.ts   # Unit tests
└── feature-name.module.ts           # Feature module (if new feature)
```

### Component Template (TypeScript)
```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

/**
 * [Feature Name] Component
 * 
 * Purpose: [Describe what this component does]
 * Features: [List key features]
 */
@Component({
  selector: 'app-[feature-name]',
  standalone: false,
  templateUrl: './[feature-name].component.html',
  styleUrls: ['./[feature-name].component.scss']
})
export class [FeatureName]Component implements OnInit, OnDestroy {
  // 1. Injected services
  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private [service]Service: [Service]Service
  ) {}

  // 2. Inputs/Outputs
  @Input() [inputName]?: string;
  @Output() [eventName] = new EventEmitter<any>();

  // 3. Form
  [featureName]Form!: FormGroup;

  // 4. State properties
  isLoading = false;
  errorMessage = '';
  private destroy$ = new Subject<void>();

  // 5. Lifecycle hooks
  ngOnInit(): void {
    this.initializeForm();
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // 6. Public methods
  onSubmit(): void {
    this.[featureName]Form.markAllAsTouched();
    if (this.[featureName]Form.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      
      const payload = this.[featureName]Form.value;
      this.[service]Service.create(payload)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => {
            console.log('Success:', response);
            this.router.navigate(['/[feature-route]']);
          },
          error: (err) => {
            this.errorMessage = err.message || 'Operation failed';
            this.isLoading = false;
          },
          complete: () => {
            this.isLoading = false;
          }
        });
    }
  }

  onCancel(): void {
    this.router.navigate(['/[parent-route]']);
  }

  isInvalid(controlName: string): boolean {
    const ctrl = this.[featureName]Form.get(controlName);
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }

  // 7. Private helpers
  private initializeForm(): void {
    this.[featureName]Form = this.fb.group({
      fieldName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      // Add all fields here
    });
  }

  private loadData(): void {
    // Load initial data if needed
  }
}
```

### HTML Template Structure
```html
<div class="[feature-name]-container">
  <!-- Header -->
  <div class="page-header">
    <h2>{{ title }}</h2>
  </div>

  <!-- Error Banner -->
  <div *ngIf="errorMessage" class="error-banner">
    {{ errorMessage }}
  </div>

  <!-- Form -->
  <form [formGroup]="[featureName]Form" (ngSubmit)="onSubmit()" class="form-container">
    <!-- Form Section -->
    <section class="form-section">
      <h3 class="section-title">GENERAL</h3>
      
      <div class="form-grid">
        <div class="form-field">
          <label for="fieldName">Field Name <span class="required">*</span></label>
          <input 
            id="fieldName"
            appFormField
            formControlName="fieldName"
            type="text"
            class="form-input"
            placeholder="Enter field name"
          />
          <span *ngIf="isInvalid('fieldName')" class="error-msg">
            Field name is required
          </span>
        </div>

        <!-- Add more fields -->
      </div>
    </section>

    <!-- Form Actions -->
    <div class="form-actions">
      <button type="button" class="btn-secondary" (click)="onCancel()" [disabled]="isLoading">
        Cancel
      </button>
      <button type="submit" class="btn-primary" [disabled]="isLoading">
        <span *ngIf="isLoading">Saving...</span>
        <span *ngIf="!isLoading">Submit</span>
      </button>
    </div>
  </form>
</div>
```

### SCSS Styles Template
```scss
.feature-name-container {
  padding: 24px;
  background: var(--bg-secondary);
  min-height: calc(100vh - var(--header-height));

  .page-header {
    margin-bottom: 24px;
    
    h2 {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }

  .error-banner {
    padding: 12px 16px;
    background: rgba(255, 107, 107, 0.1);
    border-left: 4px solid var(--danger-color);
    color: var(--danger-color);
    margin-bottom: 24px;
    border-radius: var(--radius-sm);
  }

  .form-container {
    background: var(--bg-primary);
    padding: 24px;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
  }

  .form-section {
    margin-bottom: 32px;

    .section-title {
      font-size: 13px;
      font-weight: 600;
      text-transform: uppercase;
      color: var(--text-secondary);
      margin-bottom: 16px;
      letter-spacing: 0.5px;
    }
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
  }

  .form-field {
    display: flex;
    flex-direction: column;

    label {
      font-size: 13px;
      font-weight: 500;
      color: var(--text-secondary);
      margin-bottom: 6px;

      .required {
        color: var(--danger-color);
      }
    }

    .form-input {
      height: 38px;
      padding: 0 12px;
      border: 1px solid var(--border-color);
      border-radius: var(--radius-sm);
      font-size: 14px;
      transition: all 0.2s ease;

      &:focus {
        outline: none;
        border-color: var(--primary-color);
        box-shadow: 0 0 0 3px rgba(105, 78, 214, 0.12);
      }

      &.invalid {
        border-color: var(--danger-color);
      }
    }

    .error-msg {
      font-size: 12px;
      color: var(--danger-color);
      margin-top: 4px;
    }
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid var(--border-color);

    .btn-primary {
      padding: 10px 24px;
      background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
      color: white;
      border: none;
      border-radius: var(--radius-sm);
      font-weight: 500;
      cursor: pointer;

      &:hover:not(:disabled) {
        opacity: 0.9;
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }

    .btn-secondary {
      padding: 10px 24px;
      background: transparent;
      color: var(--text-secondary);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-sm);
      font-weight: 500;
      cursor: pointer;

      &:hover:not(:disabled) {
        background: var(--bg-secondary);
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }
  }
}
```

### Service Generation Template
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, tap, finalize } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

export interface [Model] {
  id: string;
  name: string;
  // Add all fields
}

export interface [Model]Request {
  name: string;
  // Add all request fields
}

/**
 * Service for [Feature] operations
 * 
 * Handles CRUD operations for [Model] entities
 */
@Injectable({ providedIn: 'root' })
export class [Feature]Service {
  private readonly baseUrl = `${environment.apiUrl}/[endpoint]`;
  private readonly _isLoading = new BehaviorSubject<boolean>(false);
  public readonly isLoading$ = this._isLoading.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Retrieve all [models]
   */
  getAll(): Observable<[Model][]> {
    this._isLoading.next(true);
    return this.http.get<[Model][]>(this.baseUrl).pipe(
      tap(data => console.log('[Feature] fetched:', data.length)),
      catchError(this.handleError),
      finalize(() => this._isLoading.next(false))
    );
  }

  /**
   * Retrieve a single [model] by ID
   */
  getById(id: string): Observable<[Model]> {
    return this.http.get<[Model]>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create a new [model]
   */
  create(request: [Model]Request): Observable<[Model]> {
    this._isLoading.next(true);
    return this.http.post<[Model]>(this.baseUrl, request).pipe(
      tap(data => console.log('[Feature] created:', data)),
      catchError(this.handleError),
      finalize(() => this._isLoading.next(false))
    );
  }

  /**
   * Update an existing [model]
   */
  update(id: string, request: [Model]Request): Observable<[Model]> {
    this._isLoading.next(true);
    return this.http.put<[Model]>(`${this.baseUrl}/${id}`, request).pipe(
      tap(data => console.log('[Feature] updated:', data)),
      catchError(this.handleError),
      finalize(() => this._isLoading.next(false))
    );
  }

  /**
   * Delete a [model]
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      tap(() => console.log('[Feature] deleted:', id)),
      catchError(this.handleError)
    );
  }

  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
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

### Module Generation Template
```typescript
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from 'src/app/shared/shared.module';
import { [Feature]Component } from './[feature].component';
import { Create[Feature]Component } from './create-[feature]/create-[feature].component';

const routes: Routes = [
  { path: '', component: [Feature]Component },
  { path: 'create', component: Create[Feature]Component }
];

@NgModule({
  declarations: [
    [Feature]Component,
    Create[Feature]Component
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    SharedModule
  ]
})
export class [Feature]Module { }
```

## Java Code Generation

### Controller Generation Template
```java
package com.loanservice.controller;

import com.loanservice.model.[Model]Request;
import com.loanservice.service.BaseIntegrationService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for [Feature] operations
 * 
 * Provides CRUD endpoints for [Model] entities
 */
@RestController
@RequestMapping("/api/[endpoint]")
public class [Feature]Controller {

    private final ApplicationContext applicationContext;

    public [Feature]Controller(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody [Model]Request request) {
        BaseIntegrationService service = resolveService(request.getClassName());
        service.basicValidation(request);
        Object result = service.basicExecute(request);
        return ResponseEntity.ok(result);
    }

    @PutMapping
    public ResponseEntity<Object> update(@RequestBody [Model]Request request) {
        BaseIntegrationService service = resolveService(request.getClassName());
        service.basicValidation(request);
        Object result = service.basicExecute(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Object> getById(@RequestBody [Model]Request request) {
        BaseIntegrationService service = resolveService(request.getClassName());
        service.basicValidation(request);
        Object result = service.basicExecute(request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody [Model]Request request) {
        BaseIntegrationService service = resolveService(request.getClassName());
        service.basicValidation(request);
        Object result = service.basicExecute(request);
        return ResponseEntity.ok(result);
    }

    private BaseIntegrationService resolveService(String className) {
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("className field is required");
        }
        try {
            return (BaseIntegrationService) applicationContext.getBean(className);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                "No service found for className: '" + className + "'", ex);
        }
    }
}
```

### Entity Generation Template
```java
package com.loanservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.loanservice.model.YNBooleanSerializer;

/**
 * JPA Entity for [MODEL_NAME] table
 * 
 * Represents [description of what this entity stores]
 */
@Entity
@Table(name = "[TABLE_NAME]")
public class [EntityName] {

    @Id
    @Column(name = "[ID_COLUMN]", length = 24)
    private String [idField];

    @Column(name = "[FIELD_NAME]", length = 50)
    private String [fieldName];

    @Column(name = "[DATE_FIELD]")
    private LocalDate [dateField];

    @Column(name = "[BOOLEAN_FIELD]")
    @JsonSerialize(using = YNBooleanSerializer.class)
    private Boolean [booleanField] = Boolean.FALSE;

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
    public String get[IdField]() { return [idField]; }
    public void set[IdField](String [idField]) { this.[idField] = [idField]; }

    // Add getters/setters for ALL fields
}
```

### Service Generation Template
```java
package com.loanservice.service;

import com.loanservice.entity.[Entity];
import com.loanservice.model.[Model]Request;
import com.loanservice.repository.[Entity]Repository;
import com.loanservice.util.TransactionIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for [Operation] operation on [Entity]
 */
@Service("[ClassName]")
public class [ClassName] extends BaseIntegrationService {

    @Autowired
    private [Entity]Repository repository;

    @Override
    public void basicValidation([Model]Request request) {
        assertNotBlank("requiredField", request.getRequiredField());
        
        if (request.getDateField() == null) {
            throw new IllegalArgumentException("dateField is required");
        }
        
        assertMaxLength("fieldName", request.getFieldName(), 50);
    }

    @Override
    public Object basicExecute([Model]Request request) {
        [Entity] entity = new [Entity]();
        
        // Set ID (auto-generate for create)
        entity.setId(TransactionIdGenerator.generate());
        
        // Map all fields from request to entity
        entity.setFieldName(request.getFieldName());
        entity.setDateField(request.getDateField());
        entity.setBooleanField(
            request.getBooleanField() != null ? request.getBooleanField() : Boolean.FALSE
        );
        
        return repository.save(entity);
    }
}
```

## Code Generation Best Practices

### Angular
1. **Always generate spec files** — Even if empty, include test file
2. **Use appFormField directive** — On all inputs and textareas
3. **Implement OnDestroy** — Use takeUntil for subscription cleanup
4. **Type all observables** — `Observable<Model>`, never `Observable<any>`
5. **Form validation** — isInvalid() helper for every form
6. **Loading states** — isLoading boolean, disable buttons
7. **Error handling** — errorMessage string, display in template
8. **Router navigation** — Navigate after success/cancel

### Java
1. **JavaDoc comments** — For all public classes and methods
2. **Validation first** — basicValidation() before basicExecute()
3. **Null safety** — Check for null, provide defaults
4. **Consistent naming** — Bean name matches className value
5. **Transaction management** — @Transactional on custom repository methods
6. **Entity mapping** — Map ALL fields from request to entity
7. **Error messages** — Clear, user-friendly messages

### Cross-Project
1. **Consistent field names** — camelCase in TypeScript/Java
2. **Date format** — ISO 8601 (YYYY-MM-DD, YYYY-MM-DDTHH:mm:ss)
3. **Boolean serialization** — Y/N in JSON, Boolean in Java
4. **className pattern** — Always include in request for service resolution
5. **REST conventions** — POST create, PUT update, GET retrieve, DELETE delete

## Common Mistakes to Avoid

### Angular
- ❌ Subscribing in services (return Observable instead)
- ❌ Not unsubscribing (use takeUntil or async pipe)
- ❌ Using `any` type (always define interfaces)
- ❌ Missing appFormField directive
- ❌ Not marking form as touched before validation
- ❌ Hardcoding API URLs (use environment files)

### Java
- ❌ Not validating inputs
- ❌ String concatenation in queries (use parameterized)
- ❌ Exposing stack traces in errors
- ❌ Using `create-drop` in production
- ❌ Not registering service bean with correct name
- ❌ Missing @Transactional on custom repository methods

## Testing Code Generation

### Angular Unit Test Template
```typescript
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { [Feature]Service } from './[feature].service';

describe('[Feature]Service', () => {
  let service: [Feature]Service;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [[Feature]Service]
    });
    service = TestBed.inject([Feature]Service);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create [model]', () => {
    const mockResponse = { id: '123', name: 'Test' };
    const request = { name: 'Test' };

    service.create(request).subscribe(response => {
      expect(response.id).toBe('123');
    });

    const req = httpMock.expectOne(`${service['baseUrl']}`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });
});
```

### Java Unit Test Template
```java
package com.loanservice.service;

import com.loanservice.model.[Model]Request;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class [ClassName]Test {

    @Test
    void basicValidation_RequiredFieldMissing_ThrowsException() {
        [ClassName] service = new [ClassName]();
        [Model]Request request = new [Model]Request();
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.basicValidation(request);
        });
    }
    
    @Test
    void basicValidation_ValidRequest_NoException() {
        [ClassName] service = new [ClassName]();
        [Model]Request request = new [Model]Request();
        request.setRequiredField("value");
        
        assertDoesNotThrow(() -> {
            service.basicValidation(request);
        });
    }
}
```

## Summary Checklist for Code Generation

- [ ] Follow existing project structure and naming conventions
- [ ] Generate all required files (no partial implementations)
- [ ] Include proper imports and dependencies
- [ ] Add TypeScript interfaces / Java DTOs
- [ ] Implement validation and error handling
- [ ] Use environment configuration for URLs
- [ ] Apply security patterns (appFormField, input validation)
- [ ] Include loading states and user feedback
- [ ] Add comments and documentation
- [ ] Generate corresponding test files
- [ ] Ensure type safety (no `any` in TypeScript)
- [ ] Follow REST API conventions
- [ ] Use reactive patterns (RxJS, Observables)
- [ ] Implement proper cleanup (ngOnDestroy, takeUntil)
- [ ] Apply consistent styling (CSS variables)
