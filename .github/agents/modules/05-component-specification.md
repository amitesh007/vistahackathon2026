# Module 5: Component Specification Generation

## MANDATORY Pre-Creation Checkpoint

**STOP! Before creating any TypeScript or HTML file, complete this checkpoint:**

- Did you fetch Figma design context for THIS specific screen? [Yes/No]
- Did you extract button labels from Figma (not assumptions)? [Yes/No]
- Did you verify action icons in Figma? [Yes/No]
- Are you implementing based on Figma extraction or assumptions? [Figma/Assumptions]
- Did you ASK user when anything was unclear? [Yes/No/N-A]

**If ANY answer is "No" or "Assumptions" → STOP, return to Modules 1-4 and resolve.**

---

## Purpose
Generate a Component Design Specification document that describes the Angular component in full detail BEFORE any code is written. User MUST approve this spec before Module 6 begins.

This replaces the "JSON template" step used in template-engine applications. For standard Angular, the spec defines data models, form structure, template layout, and service dependencies.

## Inputs
- Aggregated design specifications (from Module 1)
- Reference structure patterns (from Module 2)
- Extracted button labels (from Module 3)
- Action icons verification (from Module 4)
- Project context confirmed in Module 0 (Angular version, UI library, state approach)

## CRITICAL PRINCIPLE

> **Reference files teach you HOW to structure Angular code.**
> **Figma design context tells you WHAT to build.**
> **NEVER invent field names, controlTypes, or layout assumptions.**

---

## Process

### Step 5.1: Define TypeScript Interfaces

For each data entity in the design, define TypeScript interfaces:

```typescript
// Derive field names from Figma label text, converted to camelCase
// "First Name" → firstName, "Date of Birth" → dateOfBirth

export interface [EntityName] {
  id?: string | number;         // include if entity has an identifier

  // Mapping of Figma label → TypeScript type:
  // text input, textarea → string
  // number, currency, percentage → number
  // date picker → Date | string (ISO 8601)
  // toggle, checkbox → boolean
  // select with known options → string
  // multi-select → string[]

  [figmaLabelCamelCase]: [TypeScript type];
}

// Request payload for POST/PUT (omit id, computed fields)
export interface Create[EntityName]Request {
  [field]: [type];
}

// API response (extends entity with metadata)
export interface [EntityName]Response extends [EntityName] {
  createdAt?: string;
  updatedAt?: string;
}
```

**If Figma shows a field but the TypeScript type is ambiguous → STOP and ASK user.**

### Step 5.2: Define Reactive Form Plan (for form screens)

For each form field from Module 1, plan the FormControl:

```typescript
FormGroup: [descriptiveName]Form = new FormGroup({

  // Field: [Figma label]
  // Type: [determined from Figma field type]
  // Required: [from Figma — marked with * or "required"]
  // Validation: [from Figma hint text or validation rules]

  [fieldName]: new FormControl<[type]>([defaultValue], {
    nonNullable: true,
    validators: [
      Validators.required,        // if marked required in Figma
      Validators.maxLength(N),    // if Figma shows max length N
      Validators.email,           // if email field type
      Validators.min(N),          // if Figma shows minimum value
      Validators.pattern(regex),  // if Figma shows format constraint
    ]
  }),

  [nextFieldName]: new FormControl<[type]>(...),
})
```

**If screen is NOT a form (table/list/dashboard)**: skip this step, mark as N/A.

### Step 5.3: Define Component Inputs / Outputs

Based on screen context from Module 0:
- `@Input() entityId?: string` — if screen is a routed edit page receiving an ID
- `@Input() config?: [ConfigType]` — if screen is embedded as a child component receiving config
- `@Output() saved = new EventEmitter<[EntityName]>()` — if screen is a dialog/modal emitting on save
- `@Output() cancelled = new EventEmitter<void>()` — if screen is a dialog/modal with cancel
- Neither if screen is a full standalone routed page

### Step 5.4: Plan Template Structure

For EACH section from Module 1, plan the HTML structure:

```
TEMPLATE PLAN — [Screen Name]Component

Root element: <div class="[screen-name]-container">
  Page header:
    - <h1> with page title: "[EXACT text from Figma Module 1]"
    - [Optional breadcrumb / back button]

  Section 1: [Section Name from Figma]
    Container: [mat-card / div.section / mat-expansion-panel]
    Content type: [form fields / data table / card list]
    Fields:
      - [Figma Label] → [formControlName="fieldName"] → [mat-form-field / input / select]
      - [Figma Label] → [formControlName="fieldName"] → [mat-datepicker / mat-select]
    Sub-sections: [if any]

  Section 2: [Section Name from Figma]
    Container: [...]
    Content: [...]

  Form footer / Action bar:
    Left: [secondary buttons — exact labels from Module 3]
    Right: [primary buttons — exact labels from Module 3]
    Icon actions: [from Module 4 findings]
```

**For each table section:**
```
  Table: [purpose]
    Columns: [list column headers — EXACT text from Figma Module 1]
    Row actions: [from Module 4 findings]
    Pagination: [YES/NO]
    Search/Filter: [YES/NO]
```

### Step 5.5: Define Service Dependencies

List every service the component will inject:

```
Services to inject into [ScreenName]Component:
  - [ScreenName]Service — API calls (created in Module 7)
  - Router — navigation on submit / cancel
  - [ActivatedRoute — only if reading route params for edit mode]
  - [MatDialog — only if screen opens dialogs]
  - [MatSnackBar — only if toast notifications needed]
  - [FormBuilder — optional alternative to new FormGroup()]
```

### Step 5.6: Define Navigation Plan

```
Route: [path confirmed in Module 0]
Mode detection:
  - Create mode: [how user arrives — route param absent / query param mode=create / parent state]
  - Edit mode: [how user arrives — route param :id / query param id=xxx / @Input() entityId]
On successful submit: [navigate to /[list-route] / emit event / navigate to success route]
On cancel: [navigate back -1 / navigate to /[list-route] / emit cancelled event]
```

### Step 5.7: Define Success State (if applicable)

If Module 0 confirmed a success page:
```
Success state type: [inline flag / separate route /[path] / dialog overlay]
Trigger: [after successful POST/PUT response]
Success message: [from Module 9 Figma — NOT yet, placeholder here]
Action after success: [stay on success view / auto-navigate after 3s / user clicks button]
```

---

## STOP Gate 2 — MANDATORY Before Module 6

**Present the complete Component Design Spec to user.**

```
🛑 STOP GATE 2: Component Specification Review

Before any TypeScript or HTML is written:
□ Component spec presented in full
□ All form fields match Figma extraction (Module 1)
□ All button labels match Module 3 extraction exactly
□ Service dependencies are confirmed
□ Navigation plan is confirmed
□ User has explicitly approved OR provided change requests

ALL must be YES (or changes addressed) to proceed to Module 6.
```

**Wait for explicit user approval. Do NOT start coding until approval is received.**

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report AFTER user approves the spec and BEFORE proceeding to Module 6.**

```
✅ MODULE 5 COMPLETION REPORT: Component Specification

1. Pre-Creation Checkpoint:
   - All checkpoint questions answered YES: [CONFIRMED]
   - Zero assumptions policy followed: [CONFIRMED]

2. TypeScript Interfaces Defined:
   - Entity interfaces: [number] → [list names]
   - Request/Response interfaces: [number]

3. Reactive Form Plan (if form screen):
   - FormGroup name: [name]
   - Total FormControls: [number]
   - Required field count: [number]
   - Custom validators: [number or "none"]

4. Component Inputs/Outputs:
   - @Input() properties: [list or "none"]
   - @Output() properties: [list or "none"]

5. Template Structure:
   - Total sections planned: [number]
   - Content types: [form / table / card / mixed]
   - Buttons planned: [number] → [exact labels match Module 3: CONFIRMED]
   - Icons planned: [number from Module 4]

6. Service Dependencies Identified:
   - [list all services]

7. Navigation Plan:
   - Route: [path]
   - On submit: [action]
   - On cancel: [action]

8. User Approval:
   - Spec presented to user: [CONFIRMED]
   - User approval received: [YES/PENDING]
   - Change requests addressed: [YES/N-A]

✅ Module 5 Status: [COMPLETE/INCOMPLETE]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 6
```

**DO NOT proceed to Module 6 until user explicitly approves the spec.**

---

## Next Module
After user approval, proceed to **Module 6: Angular Component Implementation**.
