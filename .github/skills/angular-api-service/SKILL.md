# Skill: angular-api-service

**Version**: 1.0.0
**Invoked by**: `angular-ui` agent — Module 7 (only when API integration confirmed in Module 0)
**Purpose**: Generate a well-structured Angular service with HttpClient for API integration, including error handling and loading state management.

---

## When Invoked

Invoked at Module 7 when the user confirmed API integration in Module 0.
Inputs provided by calling agent:
- `base_url_config_key`: key in `environment.ts` (e.g. `apiBaseUrl`) or a full URL (for hackathon)
- `endpoint`: relative endpoint path (e.g. `/users`, `/orders/:id`)
- `http_method`: GET / POST / PUT / PATCH / DELETE
- `entity_name`: the business entity (e.g. `User`, `Order`, `Product`)
- `state_approach`: `rxjs` / `signals` / `none`

---

## Workflow

### Step 1 — Read Existing Service Files (if any)

If the user confirmed an existing service to extend:
- Read the existing service file
- Understand current patterns (Observable vs Promise, error handling approach, import style)
- Match the existing style

If creating a new service:
- Use patterns from `angular-service-standards.instructions.md`

### Step 2 — Generate TypeScript Interfaces

If not already generated in Module 5, create request/response interfaces:

```typescript
// Request payload (for POST/PUT)
export interface Create[EntityName]Request {
  [field]: [type];
}

export interface Update[EntityName]Request {
  id: string | number;
  [field]: [type];
}

// Response
export interface [EntityName]Response {
  id: string | number;
  [field]: [type];
  createdAt?: string;
  updatedAt?: string;
}

// List response (for GET many)
export interface [EntityName]ListResponse {
  items: [EntityName]Response[];
  total: number;
  page?: number;
  pageSize?: number;
}
```

### Step 3 — Generate Service File

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, tap, finalize } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

// Import interfaces (generated in Step 2 or Module 5)
import {
  Create[EntityName]Request,
  [EntityName]Response,
  [EntityName]ListResponse,
} from '../models/[entity-name].model';

@Injectable({
  providedIn: 'root'
})
export class [ScreenName]Service {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.[base_url_config_key]}/[endpoint-base]`;

  // Loading state (use BehaviorSubject for RxJS approach)
  private readonly _isLoading = new BehaviorSubject<boolean>(false);
  readonly isLoading$ = this._isLoading.asObservable();

  // Error state
  private readonly _error = new BehaviorSubject<string | null>(null);
  readonly error$ = this._error.asObservable();

  // ─── GET (list) ────────────────────────────────────────────────────────────
  getAll(params?: Record<string, string>): Observable<[EntityName]ListResponse> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        httpParams = httpParams.set(key, value);
      });
    }

    this._isLoading.next(true);
    return this.http.get<[EntityName]ListResponse>(this.baseUrl, { params: httpParams }).pipe(
      tap(() => this._error.next(null)),
      catchError(this.handleError.bind(this)),
      finalize(() => this._isLoading.next(false))
    );
  }

  // ─── GET (single) ──────────────────────────────────────────────────────────
  getById(id: string | number): Observable<[EntityName]Response> {
    this._isLoading.next(true);
    return this.http.get<[EntityName]Response>(`${this.baseUrl}/${id}`).pipe(
      tap(() => this._error.next(null)),
      catchError(this.handleError.bind(this)),
      finalize(() => this._isLoading.next(false))
    );
  }

  // ─── POST (create) ─────────────────────────────────────────────────────────
  create(payload: Create[EntityName]Request): Observable<[EntityName]Response> {
    this._isLoading.next(true);
    return this.http.post<[EntityName]Response>(this.baseUrl, payload).pipe(
      tap(() => this._error.next(null)),
      catchError(this.handleError.bind(this)),
      finalize(() => this._isLoading.next(false))
    );
  }

  // ─── PUT (update) ──────────────────────────────────────────────────────────
  update(id: string | number, payload: Partial<Create[EntityName]Request>): Observable<[EntityName]Response> {
    this._isLoading.next(true);
    return this.http.put<[EntityName]Response>(`${this.baseUrl}/${id}`, payload).pipe(
      tap(() => this._error.next(null)),
      catchError(this.handleError.bind(this)),
      finalize(() => this._isLoading.next(false))
    );
  }

  // ─── DELETE ────────────────────────────────────────────────────────────────
  delete(id: string | number): Observable<void> {
    this._isLoading.next(true);
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      tap(() => this._error.next(null)),
      catchError(this.handleError.bind(this)),
      finalize(() => this._isLoading.next(false))
    );
  }

  // ─── Error Handler ─────────────────────────────────────────────────────────
  private handleError(error: HttpErrorResponse): Observable<never> {
    let message = 'An unexpected error occurred.';

    if (error.status === 0) {
      // Network error
      message = 'Network error — please check your connection.';
    } else if (error.status === 400) {
      message = error.error?.message ?? 'Bad request. Please check your input.';
    } else if (error.status === 401) {
      message = 'Unauthorized. Please log in again.';
    } else if (error.status === 403) {
      message = 'You do not have permission to perform this action.';
    } else if (error.status === 404) {
      message = 'The requested resource was not found.';
    } else if (error.status >= 500) {
      message = 'Server error. Please try again later.';
    }

    this._error.next(message);
    return throwError(() => new Error(message));
  }
}
```

### Step 4 — Trim to Confirmed Operations

ONLY include methods for the HTTP operations confirmed in Module 0.
- If only GET + POST confirmed: remove `update()` and `delete()`
- If hackathon/no-auth context: remove 401/403 handling or simplify

### Step 5 — Generate Models File

Create `[entity-name].model.ts` in a `models/` folder alongside the feature:

```typescript
export interface [EntityName] {
  [field]: [type];
}

export interface Create[EntityName]Request {
  [field]: [type];
}

export interface [EntityName]Response extends [EntityName] {
  id: string | number;
}
```

### Step 6 — Signals Variant (if `state_approach === 'signals'`)

If the user confirmed Angular Signals for state management, replace `BehaviorSubject` with signals:

```typescript
import { signal, computed } from '@angular/core';

// Replace BehaviorSubject state:
private readonly _isLoading = signal(false);
readonly isLoading = computed(() => this._isLoading());

private readonly _error = signal<string | null>(null);
readonly error = computed(() => this._error());

// In methods, replace .next() with set():
this._isLoading.set(true);
this._error.set(null);
// In finalize: this._isLoading.set(false)
```

---

## Output Contract

Returns:
- Path of `[screen-name].service.ts`
- Path of `[entity-name].model.ts` (if created)
- List of methods generated

---

## Error Handling Rules

1. NEVER swallow errors silently — always propagate via `throwError()`
2. NEVER hardcode API URLs — always use `environment.[key]`
3. ALWAYS use `finalize` to reset loading state (even on error)
4. For hackathons where environment config doesn't exist yet: use a `const BASE_URL = 'http://localhost:3000'` constant at the top of the service, with a `// TODO: move to environment.ts` comment
