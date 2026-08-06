# Module 2: Reference Pattern Analysis

## Purpose
Study existing Angular component files (if provided) to understand structure patterns WITHOUT copying content (labels, buttons, text).

## CRITICAL WARNING
**Reference files are for STRUCTURE ONLY, NOT content!**
- Learn: Component class structure, template patterns, service injection style, error handling approach
- Do NOT copy: label text, button text, field names, any user-facing strings

---

## Process

### Step 2.1: Determine Reference Source

**Ask the user (if not already answered in Module 0):**
```
Do you have existing Angular component files I should study for patterns?

Option A: Yes — please provide the file paths (component.ts + HTML + service.ts)
Option B: No — I'll use generic Angular best-practice patterns
Option C: Yes — here's the GitHub/project path to find similar screens
```

If user says **No reference files**: proceed using patterns from `.github/instructions/angular-component-standards.instructions.md` as the reference baseline. Skip to Step 2.3.

### Step 2.2: Read ALL Reference File Types

**If reference files provided, read ALL of the following for each reference screen:**

1. `[screen-name].component.ts` — class structure, inject() calls, lifecycle hooks, form setup
2. `[screen-name].component.html` — template selector, control flow syntax, form binding approach
3. `[screen-name].component.scss` — CSS class naming, BEM structure, variable usage
4. `[screen-name].service.ts` — HttpClient patterns, Observable vs Promise, state management

**Mandatory answers to extract from HTML file:**
- What is the root container class name? (e.g. `.feature-container`, `.screen-wrapper`)
- Is Angular control flow used? (`@if`, `@for`) or legacy (`*ngIf`, `*ngFor`)?
- How are form errors displayed? (mat-error / alert div / inline span)
- What UI library components are used? (mat-form-field / p-inputText / plain input)

**Common mistake to avoid:** Reading only TypeScript files and missing the actual template patterns.

### Step 2.3: Analyze Structure Patterns (ONLY)

Read at least 1-2 reference files (or the instruction file baseline) to understand:

**Component Class Structure:**
- Are services injected via `inject()` or constructor?
- Are signals or BehaviorSubjects used for state?
- How is `DestroyRef` / `takeUntilDestroyed` used?
- What lifecycle hooks are implemented (`OnInit`, `OnDestroy`, `AfterViewInit`)?

**Reactive Form Patterns:**
- Is `FormGroup` typed or untyped?
- How are validators applied (inline vs `Validators.compose`)?
- How is `markAllAsTouched` triggered on invalid submit?

**Template Patterns:**
- What Angular control flow version? (`@if` / `*ngIf`)
- How are loading states shown (spinner / skeleton / disabled button)?
- How are validation errors shown (mat-error / custom div / `aria-live`)?
- How are async data streams consumed (`async` pipe vs manual subscribe)?

**Service Patterns:**
- Does the service expose Observables or Promises?
- Is `BehaviorSubject` or `signal` used for local state?
- How is error handling structured (`catchError` / `try-catch`)?
- Does the service manage loading state internally?

**SCSS Patterns:**
- BEM naming: `.[block]__[element]--[modifier]`
- CSS custom properties for theming
- Responsive breakpoints used

### Step 2.4: Document What NOT to Copy

Identify and explicitly note the following as "content, not structure" — do NOT use them:
- Any display label text
- Any button label text
- Any error message wording
- Any route paths (use the route from Module 0 confirmation instead)
- Any API endpoint URLs

### Step 2.5: Identify Key Patterns to Replicate

Based on reference study, document the patterns to use for THIS screen:

```
Angular version: [17/18/19/20/21]
Standalone components: [YES/NO]
UI library: [Angular Material / PrimeNG / plain SCSS]

Component patterns to replicate:
  - Injection style: [inject() function]
  - Form approach: [ReactiveFormsModule / FormBuilder]
  - State management: [BehaviorSubject / signal / none]
  - Lifecycle: [OnInit / OnDestroy / both / neither]
  - Subscription management: [takeUntilDestroyed / async pipe / manual]

Template patterns to replicate:
  - Control flow: [@if @for / *ngIf *ngFor]
  - Form binding: [formGroup + formControlName / standalone formControl]
  - Error display: [mat-error / custom div / aria-live]
  - Loading indicator: [mat-spinner / spinner class / disabled button only]

Service patterns to replicate:
  - Return type: [Observable<T> / Promise<T>]
  - Error handling: [catchError + throwError / try-catch]
  - Loading state: [BehaviorSubject / signal / none]
```

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report before proceeding to Module 3.**

```
✅ MODULE 2 COMPLETION REPORT: Reference Pattern Analysis

1. Reference Source:
   - Option used: [A: files provided / B: no reference / C: project path]
   - Files studied: [list or "none — using instruction baseline"]

2. Structure Patterns Learned:
   - Component injection style: [inject() / constructor]
   - Form approach: [ReactiveFormsModule / FormBuilder]
   - State management: [signal / BehaviorSubject / none]
   - Template control flow: [@if @for / *ngIf *ngFor]
   - Error display: [mat-error / custom / aria-live]
   - Loading pattern: [mat-spinner / disabled button / skeleton]

3. Key Patterns to Apply to This Screen:
   - [pattern 1]
   - [pattern 2]
   (list all non-obvious decisions)

4. Content Commitment:
   - Will NOT copy any label or button text: [CONFIRMED]
   - Will NOT copy any route paths: [CONFIRMED]
   - Will NOT copy any API URLs: [CONFIRMED]
   - Reference used for STRUCTURE ONLY: [CONFIRMED]

✅ Module 2 Status: [COMPLETE/INCOMPLETE]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 3
```

**DO NOT proceed to Module 3 until user confirms this report.**

---

## Next Module
Proceed to **Module 3: Button Label Extraction**.
