# Module 7: Angular Service Implementation

## Purpose
Create the Angular service with HttpClient integration, state management, error handling, and data transformation.

## Load Instruction Files
**Load now:**
- `.github/instructions/angular-service-standards.instructions.md`
- `.github/instructions/angular-api-integration.instructions.md`

## Inputs
- Component requirements from Module 6
- API integration details from Module 0 (endpoint URLs, HTTP methods, operations confirmed)
- TypeScript interfaces from Module 5 (Component Spec)

---

## CRITICAL: API Integration — STOP and ASK User First

**If API integration was confirmed in Module 0 but the following are unknown, STOP and ask:**

For EACH HTTP operation (GET / POST / PUT / DELETE):
```
Before implementing any API method, I need:
1. The endpoint URL or path (e.g. /api/users or /api/users/:id)
2. The HTTP method (GET / POST / PUT / PATCH / DELETE)
3. The request payload structure (if POST/PUT) — fields and types
4. The response structure — what does the API return?
5. Is there an existing environment config key for the base URL?
   (e.g. environment.apiBaseUrl)
```

**If user does NOT provide API information:**
- Implement stub methods with `// TODO: replace with real API call` comments
- Return `of(null)` or `of([])` as Observable stubs
- Service is still created to maintain architecture consistency

---

## Process

### Step 7.1: Determine Service Location

Create service at path confirmed in Module 0:
```
[services-path]/[screen-name].service.ts
```

Also create models file if not already created in Module 5:
```
[features-path]/[screen-name]/models/[entity-name].model.ts
```

### Step 7.2: If API Integration Confirmed — Invoke `angular-api-service` Skill

**Invoke the `angular-api-service` skill (`.github/skills/angular-api-service/SKILL.md`) now.**

Pass to the skill:
- `base_url_config_key`: environment config key (e.g. `apiBaseUrl`)
- `endpoint`: relative endpoint path (e.g. `/users`, `/orders`)
- `http_method`: the confirmed operation(s)
- `entity_name`: business entity name from Module 5 interfaces
- `state_approach`: from Module 0 (`rxjs` / `signals` / `none`)

The skill generates the complete service file following `angular-service-standards.instructions.md`.

**After skill completes:** Review generated code, adapt any method signatures to match the exact field names from Module 5 interfaces.

### Step 7.3: If NO API Integration — Generate Stub Service

```typescript
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { [EntityName] } from '../[screen-name]/models/[entity-name].model';

@Injectable({ providedIn: 'root' })
export class [ScreenName]Service {

  // TODO: Replace stubs with real HttpClient calls when API is available

  getAll(): Observable<[EntityName][]> {
    return of([]);  // TODO: implement GET /[endpoint]
  }

  getById(id: string): Observable<[EntityName] | null> {
    return of(null);  // TODO: implement GET /[endpoint]/:id
  }

  create(payload: Create[EntityName]Request): Observable<[EntityName]> {
    return of({ id: Date.now().toString(), ...payload } as [EntityName]);  // TODO: implement POST /[endpoint]
  }

  update(id: string, payload: Partial<Create[EntityName]Request>): Observable<[EntityName]> {
    return of({ id, ...payload } as [EntityName]);  // TODO: implement PUT /[endpoint]/:id
  }

  delete(id: string): Observable<void> {
    return of(undefined);  // TODO: implement DELETE /[endpoint]/:id
  }
}
```

### Step 7.4: Add Dropdown / Lookup Data Methods

If the screen has `<select>` fields, add methods to load the options:

**For static options** (known at build time):
```typescript
readonly [fieldName]Options = [
  { value: 'option1', label: 'Option 1 Label' },
  { value: 'option2', label: 'Option 2 Label' },
];
```

**For dynamic options** (loaded from API):
```typescript
get[FieldName]Options(): Observable<SelectOption[]> {
  return this.http.get<SelectOption[]>(`${this.baseUrl}/[lookup-endpoint]`).pipe(
    catchError(() => of([]))
  );
}
```

### Step 7.5: Service Registration

For standalone Angular (confirmed in Module 0), `providedIn: 'root'` is sufficient — NO additional registration steps required.

For NgModule-based apps: ensure the service is listed in `providers` of the appropriate module (ask user which module if unclear).

**There is NO ServiceFactoryService, NO SharedLibraryService, NO CoreService registration needed.** Standard Angular DI handles this automatically.

### Step 7.6: Validate Service Methods Match Component Usage

Cross-check that every service method called in Module 6's component exists in this service:
- Component calls `this.[screenName]Service.create(payload)` → service has `create()` method
- Component calls `this.[screenName]Service.getById(id)` → service has `getById()` method
- Return types match the component's subscription handler types

If a mismatch is found → fix service signature before proceeding.

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report before proceeding to Module 8.**

```
✅ MODULE 7 COMPLETION REPORT: Service Implementation

1. Files Created:
   - [path]/[screen-name].service.ts → [N] lines
   - [path]/models/[entity-name].model.ts → [N] lines (if created)

2. API Integration:
   - API integration confirmed: [YES/NO]
   - If YES: skill `angular-api-service` invoked: [YES/NO]
   - If NO: stub service generated: [YES/NO]

3. Methods Implemented:
   - getAll() / getById(): [IMPLEMENTED/STUB/N-A]
   - create(): [IMPLEMENTED/STUB/N-A]
   - update(): [IMPLEMENTED/STUB/N-A]
   - delete(): [IMPLEMENTED/STUB/N-A]
   - Dropdown/lookup methods: [number or "none"]

4. State Management:
   - Loading state: [BehaviorSubject / signal / none]
   - Error state: [BehaviorSubject / signal / none]
   - Exposed as Observable/readonly: [CONFIRMED/N-A]

5. Error Handling:
   - catchError in all HTTP calls: [CONFIRMED/N-A]
   - finalize for loading reset: [CONFIRMED/N-A]
   - Error messages propagated to component: [CONFIRMED]

6. Service Registration:
   - providedIn: 'root': [CONFIRMED]
   - No additional registration required: [CONFIRMED]

7. Component/Service Method Alignment:
   - All component service calls have matching methods: [CONFIRMED]
   - Return types align: [CONFIRMED]

8. CHECKPOINT Results:
   - No console.log: [CONFIRMED]
   - No hardcoded URLs (environment config used): [CONFIRMED/TODOs marked]
   - No 'any' types (typed interfaces used): [CONFIRMED]

✅ Module 7 Status: [COMPLETE/INCOMPLETE]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 8
```

**DO NOT proceed to Module 8 until user confirms this report.**

---

## Next Module
Proceed to **Module 8: Routing Configuration**.
