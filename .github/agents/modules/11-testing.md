# Module 11: Testing

## Purpose
Generate Jest unit tests for the component and service created in Modules 6–7. Verify that the implementation meets the minimum coverage threshold configured in Module 0.

---

## MANDATORY STOP GATE

**Before generating tests, confirm:**
1. Module 6 component is COMPLETE and all Module 6 stop gate items passed? [Yes]
2. Module 7 service is COMPLETE? [Yes]
3. Module 10 i18n is COMPLETE (or confirmed as hackathon mode)? [Yes]
4. Module 0 testing configuration is known (Jest / coverage threshold)? [Yes]

**If any answer is "No" → resolve that module first before generating tests.**

---

## Coverage Target

From Module 0:
- Default coverage threshold: **80% statements, branches, functions, lines**
- Custom threshold (if set in Module 0): **[value]%**
- Coverage tool: **Jest** (with `--coverage` flag)

---

## Process

### Step 11.1: Identify Test Targets

**Component tests must cover:**
- Component creation (smoke test)
- Initial form state (default values, validators)
- Submit handler: valid form → service called + navigation
- Submit handler: invalid form → service NOT called + errors shown
- Cancel handler: navigation triggered
- Edit mode: `ngOnInit` with route param → service.getById called + form patched
- Success state: `isSuccess` flag toggle (if inline success in Module 9)
- Loading state: `isLoading` set/unset during async operations
- Error state: `errorMessage` set on service error

**Service tests must cover:**
- Service creation
- GET method: HttpClient called with correct URL, maps response to entity
- POST method: HttpClient called with correct URL + payload, returns entity
- PUT method: HttpClient called with correct URL + id + payload
- DELETE method: HttpClient called with correct URL + id
- Error handling: catchError propagates observable error
- State management: `isLoading$` / signal updates during request lifecycle (if implemented)

---

### Step 11.2: Invoke `jest-generator` Skill — Component Tests

**Invoke the `jest-generator` skill (`.github/skills/jest-generator/SKILL.md`) for the component.**

Pass to the skill:
- `target_file`: `[path]/[screen-name].component.ts`
- `test_file`: `[path]/[screen-name].component.spec.ts`
- `component_class_name`: `[ScreenName]Component`
- `services_used`: list of services injected (e.g. `[ScreenName]Service`, `Router`, `ActivatedRoute`)
- `form_controls`: list of FormControl names (from Module 5 spec)
- `has_edit_mode`: `true` / `false`
- `has_inline_success`: `true` / `false`
- `coverage_threshold`: configured value from Module 0

The skill generates the complete `.spec.ts` file following Jest + ng-mocks patterns.

---

### Step 11.3: Invoke `jest-generator` Skill — Service Tests

**Invoke the `jest-generator` skill again for the service.**

Pass to the skill:
- `target_file`: `[path]/[screen-name].service.ts`
- `test_file`: `[path]/[screen-name].service.spec.ts`
- `service_class_name`: `[ScreenName]Service`
- `http_methods`: list of HTTP operations (e.g. `GET`, `POST`, `PUT`, `DELETE`)
- `entity_interface`: `[EntityName]` (from Module 5 interfaces)
- `has_state_management`: `true` / `false`
- `is_stub_service`: `true` if no real HttpClient (Module 7 stub mode)

---

### Step 11.4: Review Generated Tests

After skill completes, check generated spec files:

**Component spec checklist:**
```
□ TestBed.configureTestingModule imports ReactiveFormsModule
□ MockService from ng-mocks used for all injected services
□ RouterTestingModule or provideRouter([]) included
□ Component instance created in beforeEach
□ Form submit test: form.setValue() → triggerSubmit() → verify service.create called
□ Invalid form test: invalid state → submit → service.create NOT called
□ Cancel test: onCancel() → verify Router.navigate called with expected route
□ Loading state test: isLoading true during, false after
□ Error state test: service throws → errorMessage set
□ No console.error / console.warn in tests
□ All describe/it blocks have meaningful names
```

**Service spec checklist:**
```
□ HttpClientTestingModule (or provideHttpClientTesting()) included
□ HttpTestingController used for request flushing
□ afterEach: httpController.verify() — no outstanding requests
□ GET test: service.getAll() → request flushed with mock data → entity[] returned
□ POST test: service.create(payload) → request flushed → entity returned
□ Error test: request flushed with error → observable errors with message
□ State tests: isLoading$ updates correctly (if state management used)
```

---

### Step 11.5: Run Tests and Verify Coverage

Run tests with coverage:

```bash
# For the specific feature folder:
npx jest --testPathPattern="[screen-name]" --coverage

# Or use the npm script from Module 0 project config:
npm run test:coverage -- --testPathPattern="[screen-name]"
```

**Expected output:**
```
PASS  src/app/features/[screen-name]/[screen-name].component.spec.ts
PASS  src/core/services/[screen-name].service.spec.ts

Coverage summary:
  Statements   : [N]% ( >= [threshold]% required )
  Branches     : [N]% ( >= [threshold]% required )
  Functions    : [N]% ( >= [threshold]% required )
  Lines        : [N]% ( >= [threshold]% required )
```

**If coverage is BELOW threshold:**
1. Identify uncovered branches/functions from the coverage report
2. Add targeted tests for uncovered paths
3. Re-run until threshold is met

**If tests FAIL:**
1. Read the error output
2. Fix the spec file (mock setup, import issue, async timing)
3. Do NOT relax the threshold to make tests pass

---

### Step 11.6: Register Pattern (Module 11.5 — Optional)

If this screen implements a reusable pattern that other screens could follow:

**Open `PATTERN_REGISTRY.md` and add an entry:**
```markdown
## [Pattern Name] (e.g. "Create-Entity Form with Inline Success")

**Tags**: `form-screen`, `create-mode`, `inline-success`, `angular-material`

**Source Screen**: `[ScreenName]Component` at `[relative path]`

**What it demonstrates**:
- [specific pattern — e.g. "FormGroup with cross-field validation and inline success toggle"]

**Reuse guidance**:
- Copy `[ScreenName]Component` as a starting template
- Replace: entity interface, service calls, form fields, Figma-exact labels
- Keep: submit/cancel pattern, loading/error states, `takeUntilDestroyed`
```

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report — this is the FINAL module.**

```
✅ MODULE 11 COMPLETION REPORT: Testing

1. jest-generator Skill Invocations:
   - Component spec: [INVOKED / MANUALLY WRITTEN]
     - File: [path]/[screen-name].component.spec.ts
     - Test cases written: [N]
   - Service spec: [INVOKED / MANUALLY WRITTEN]
     - File: [path]/[screen-name].service.spec.ts
     - Test cases written: [N]

2. Test Results:
   - Component spec: [ALL PASS / N failures]
   - Service spec: [ALL PASS / N failures]

3. Coverage Results (vs [threshold]% threshold):
   - Statements : [N]% — [PASS/FAIL]
   - Branches   : [N]% — [PASS/FAIL]
   - Functions  : [N]% — [PASS/FAIL]
   - Lines      : [N]% — [PASS/FAIL]

4. Pattern Registry:
   - New pattern registered: [YES — pattern name / NO]

5. All Modules Summary:
   Module 0  — Project Context          : ✅
   Module 1  — Figma Extraction         : ✅
   Module 2  — Reference Analysis       : ✅
   Module 3  — Button Labels            : ✅
   Module 4  — Action Icons             : ✅
   Module 5  — Component Spec          : ✅ (approved by user)
   Module 6  — Component Implementation: ✅
   Module 7  — Service Implementation  : ✅
   Module 8  — Routing Configuration   : ✅
   Module 9  — Success Page            : ✅ / N-A
   Module 10 — i18n Translations       : ✅
   Module 11 — Testing                 : ✅

6. Outstanding Items / Next Steps:
   - [list any items left for user: pending translations, API integration TODOs, etc.]
   - [or "none — implementation complete"]

✅ ALL 11 MODULES COMPLETE — Implementation ready for review.
```

---

## Implementation Complete

Congratulations — the screen is fully implemented. Recommended next steps:
1. Run a full build (`ng build` or `npm run build`) to confirm no compilation errors
2. Test the feature in a running dev server (`ng serve` or `npm start`)
3. Address any pending `// TODO:` API integration stubs with real endpoints
4. If i18n was deferred (hackathon mode), extract `i18n-TODO:` markers when translations are added
