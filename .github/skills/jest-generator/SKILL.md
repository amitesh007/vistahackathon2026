# Skill: jest-generator

**Version**: 1.0.0
**Type**: Sub-Agent skill (invoked via `runSubagent`)
**Invoked by**: `angular-ui` agent — Module 11b (only after `implementation-verifier` passes)
**Purpose**: Generate production-quality Jest unit tests for Angular component and service files.

---

## When Invoked

Invoked at Module 11b. Pre-condition: `implementation-verifier` must have passed (Exit 0).
Run twice — once for the component `.ts` file, once for the service `.ts` file.

---

## Input (passed by calling agent)

```
- component_path: string        // e.g. src/app/features/create-user/create-user.component.ts
- service_path: string          // e.g. src/app/core/services/create-user.service.ts
- feature_name: string          // e.g. "create-user"
- story_reference: string       // e.g. "VISTA-42" or "none"
- coverage_threshold: number    // e.g. 80 (from Module 0 context)
- angular_version: number       // e.g. 19
- ui_library: string            // e.g. "angular-material" / "primeng" / "plain"
```

---

## Workflow

### Step 1 — Read Source Files

Read the component `.ts` and service `.ts` files completely.
From component, identify:
- Class name, selector
- All `@Input()` / `@Output()` properties
- All reactive form controls and their validators
- All public/protected methods
- Service dependencies injected
- Router usage

From service, identify:
- Injectable scope
- All public methods and their signatures
- HttpClient usage (endpoints, HTTP methods)
- Observable / Promise return types

### Step 2 — Generate Component Spec

Create `[screen-name].component.spec.ts` alongside the component file.

**Structure:**

```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { [ScreenName]Component } from './[screen-name].component';
import { [ScreenName]Service } from '../../core/services/[screen-name].service';

describe('[ScreenName]Component', () => {
  let component: [ScreenName]Component;
  let fixture: ComponentFixture<[ScreenName]Component>;
  let mockService: jest.Mocked<[ScreenName]Service>;
  let mockRouter: jest.Mocked<Router>;

  beforeEach(async () => {
    mockService = {
      [method]: jest.fn().mockReturnValue(of([mockResponse])),
    } as any;

    mockRouter = {
      navigate: jest.fn(),
    } as any;

    await TestBed.configureTestingModule({
      imports: [[ScreenName]Component],
      providers: [
        { provide: [ScreenName]Service, useValue: mockService },
        { provide: Router, useValue: mockRouter },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent([ScreenName]Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // --- Initialization ---
  describe('initialization', () => {
    it('should create component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize form with empty values', () => {
      expect(component.form.value).toEqual({
        [field1]: null,   // or '' or false — match actual defaults
        [field2]: null,
        // ...
      });
    });

    it('should have form invalid on init', () => {
      expect(component.form.invalid).toBe(true);
    });
  });

  // --- Validation ---
  describe('form validation', () => {
    // For each required field:
    it('should mark [field] as invalid when empty', () => {
      component.form.controls.[field].setValue('');
      expect(component.form.controls.[field].hasError('required')).toBe(true);
    });

    it('should mark form as valid when all required fields filled', () => {
      component.form.setValue({
        [field1]: '[valid value]',
        // ...
      });
      expect(component.form.valid).toBe(true);
    });
  });

  // --- Submit ---
  describe('onSubmit()', () => {
    it('should not call service when form is invalid', () => {
      component.onSubmit();
      expect(mockService.[method]).not.toHaveBeenCalled();
    });

    it('should mark all controls touched when form invalid', () => {
      component.onSubmit();
      Object.values(component.form.controls).forEach(ctrl => {
        expect(ctrl.touched).toBe(true);
      });
    });

    it('should call service with form values when form valid', () => {
      component.form.setValue({ [field1]: '[value]', /* ... */ });
      component.onSubmit();
      expect(mockService.[method]).toHaveBeenCalledWith(
        expect.objectContaining({ [field1]: '[value]' })
      );
    });

    it('should navigate on successful submit', () => {
      component.form.setValue({ [field1]: '[value]', /* ... */ });
      mockService.[method].mockReturnValue(of({}));
      component.onSubmit();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/[success-route]']);
    });

    it('should set errorMessage on service error', () => {
      component.form.setValue({ [field1]: '[value]', /* ... */ });
      mockService.[method].mockReturnValue(throwError(() => new Error('Server error')));
      component.onSubmit();
      expect(component['errorMessage']).toBe('Server error');
    });

    it('should set isLoading true while submitting', () => {
      // test loading state
    });
  });

  // --- Cancel ---
  describe('onCancel()', () => {
    it('should navigate back on cancel', () => {
      component.onCancel();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/[cancel-route]']);
    });
  });
});
```

### Step 3 — Generate Service Spec

Create `[screen-name].service.spec.ts` alongside the service file.

**Structure:**

```typescript
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { [ScreenName]Service } from './[screen-name].service';
import { environment } from '../../../environments/environment';

describe('[ScreenName]Service', () => {
  let service: [ScreenName]Service;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [[ScreenName]Service],
    });
    service = TestBed.inject([ScreenName]Service);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // For each HTTP method in the service:
  describe('[method]()', () => {
    it('should [HTTP method] to correct endpoint', () => {
      const mockPayload = { [field]: '[value]' };
      const mockResponse = { id: '1', ...mockPayload };

      service.[method](mockPayload).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${environment.apiBaseUrl}/[endpoint]`);
      expect(req.request.method).toBe('[HTTP_METHOD]');
      req.flush(mockResponse);
    });

    it('should handle [HTTP method] error gracefully', () => {
      const mockPayload = { [field]: '[value]' };
      let errorCaught = false;

      service.[method](mockPayload).subscribe({
        error: () => { errorCaught = true; }
      });

      const req = httpMock.expectOne(`${environment.apiBaseUrl}/[endpoint]`);
      req.flush('Error', { status: 500, statusText: 'Server Error' });

      expect(errorCaught).toBe(true);
    });
  });
});
```

### Step 4 — Adapt to Actual Code

Replace all placeholders (`[field]`, `[method]`, `[endpoint]`, etc.) with actual values read from the source files in Step 1. The templates above are structural guides only.

Aim for:
- Coverage of every `public` and `protected` method
- Every validation rule tested (each `Validators.*`)
- Both success and error paths for every HTTP call
- All `@Input()` binding scenarios (if component has inputs)

### Step 5 — Coverage Check

After generating spec files, estimate coverage:
- Count distinct code paths in source files
- Verify spec file has a test for each branch

Report estimated coverage per metric:
```
Estimated coverage:
  Statements: ~[N]%
  Branches:   ~[N]%
  Functions:  ~[N]%
  Lines:      ~[N]%

Threshold ([coverage_threshold]%): [MET / NOT MET]
```

If NOT MET: add additional tests for uncovered branches before reporting complete.

---

## Output Contract

Returns:
```
- component_spec_path: [path]/[screen-name].component.spec.ts
- service_spec_path: [path]/[screen-name].service.spec.ts
- estimated_coverage: { statements, branches, functions, lines }
- threshold_met: boolean
```

---

## Hard Rules

1. NEVER import from `@loaniq/*` or any project-specific library — this is a generic skill
2. NEVER use `TestBed.inject` on a real service — always mock with `jest.fn()`
3. NEVER write specs that test the framework (e.g. "Angular creates a FormControl") — test component behavior
4. ALWAYS test error paths, not just happy paths
5. If stub service (no API) was generated: skip HTTP specs, test that methods exist and return expected types
