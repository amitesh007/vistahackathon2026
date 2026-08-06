# Skill: component-generator

**Version**: 1.0.0
**Invoked by**: `angular-ui` agent — Module 5 (Phase 1: Spec) and Module 6 (Phase 2: Code)
**Purpose**: Generate Angular component specification then code from a Design Spec document.

---

## Two Phases

This skill operates in two distinct phases:

| Phase | Module | Input | Output |
|-------|--------|-------|--------|
| Phase 1: Spec | Module 5 | Design Spec from figma-extractor | Component Design Spec (Markdown) |
| Phase 2: Code | Module 6 | Approved Component Spec | `.ts` + `.html` + `.scss` files |

---

## Phase 1: Component Specification

### Step 1 — Read Design Spec
- Load the Design Spec document produced by `figma-extractor` (Module 1).
- Note the confirmed Angular version, UI library, form approach (Module 0).

### Step 2 — Define TypeScript Interfaces

For each data entity in the design, generate an interface:

```typescript
// Use the exact field names from Figma labels, converted to camelCase
export interface [EntityName] {
  id?: string | number;         // include if entity has an identifier
  [fieldName]: [TypeScript type]; // derived from Figma field type
  // string → string
  // number, currency, percentage → number
  // date picker → Date | string
  // toggle/checkbox → boolean
  // select with known options → string (or union type if options are known)
  // multi-select → string[]
}
```

### Step 3 — Plan Reactive Form (if form screen)

```typescript
// For each form field from Design Spec:
// - Determine the FormControl type
// - Determine required validator (if field is marked required in Figma)
// - Determine pattern/max/min validators (if Figma shows format hints)

FormGroup: [formGroupName]Form = new FormGroup({
  [fieldName]: new FormControl<[type]>([defaultValue], {
    validators: [Validators.required?, Validators.maxLength(N)?, ...]
  }),
  ...
})
```

### Step 4 — Define Component Inputs/Outputs

Based on screen context (from Module 0):
- `@Input()` if screen receives data from parent (e.g. entity ID for edit mode)
- `@Output()` if screen emits events to parent (e.g. close dialog event)
- Neither if screen is a full routed page

### Step 5 — Template Structure Plan

For each section in the Design Spec, plan the HTML structure:

```
Section: [Name]
  Container: [div.section-container / mat-card / mat-expansion-panel]
  Content: [mat-form-field list / mat-table / custom card]
  Sub-sections: [if nested]

Footer:
  Buttons: [Button 1 Label] (primary) | [Button 2 Label] (secondary)
  Alignment: [right-aligned / full-width / centered]
```

### Step 6 — Service Dependencies

List services the component will inject:
```
- [ScreenName]Service — API calls for this feature
- Router — navigation on submit/cancel
- FormBuilder — reactive form construction
- [SnackBar / Toast service] — if notifications needed
- [Dialog service] — if modals needed
```

### Step 7 — Navigation Plan

```
Route: [confirmed path from Module 0]
Create mode trigger: [how user lands here]
Edit mode trigger: [route param :id / query param / state — or N/A]
On successful submit: [navigate to /path OR emit event OR show inline success]
On cancel: [navigate to /path OR close dialog]
```

### Output — Component Design Spec

Produce a Markdown document formatted exactly as:

```markdown
# Component Spec: [ScreenName]Component
Generated from Design Spec: [screen name]
Angular version: [version] | Standalone: [Y/N] | UI Library: [library]

## TypeScript Interfaces
\`\`\`typescript
[interfaces]
\`\`\`

## Reactive Form Plan
\`\`\`typescript
[FormGroup definition with validators]
\`\`\`

## Component Inputs/Outputs
- @Input(): [list or "none"]
- @Output(): [list or "none"]

## Template Structure
[Section-by-section layout plan]

## Service Dependencies
[List of services to inject]

## Navigation Plan
- Route: [path]
- Create mode: [trigger]
- Edit mode: [trigger or "N/A"]
- On submit: [action]
- On cancel: [action]

## Open Questions
[Any spec items requiring user confirmation before coding begins]
```

**Present spec to user. WAIT for explicit approval before Phase 2.**

---

## Phase 2: Code Generation

### Step 1 — Generate `.component.ts`

Generate following `angular-component-standards.instructions.md`:

```typescript
import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
// ... other imports from UI library

@Component({
  selector: 'app-[screen-name]',
  standalone: true,           // if standalone confirmed
  imports: [
    ReactiveFormsModule,
    // RouterModule, MatFormFieldModule, etc. — only what's used
  ],
  templateUrl: './[screen-name].component.html',
  styleUrl: './[screen-name].component.scss'
})
export class [ScreenName]Component implements OnInit {
  private readonly router = inject(Router);
  private readonly [screenName]Service = inject([ScreenName]Service);
  private readonly destroyRef = inject(DestroyRef);

  // Reactive form
  protected readonly form: FormGroup = new FormGroup({
    // [approved form plan]
  });

  // State
  protected isLoading = false;
  protected errorMessage = '';

  ngOnInit(): void {
    // Load initial data if edit mode
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    this.[screenName]Service.[method](this.form.value)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.router.navigate(['/[success-route]']),
        error: (err) => {
          this.errorMessage = err.message ?? 'An error occurred.';
          this.isLoading = false;
        }
      });
  }

  protected onCancel(): void {
    this.router.navigate(['/[cancel-route]']);
  }
}
```

### Step 2 — Generate `.component.html`

- Use Angular control flow (`@if`, `@for`, `@switch`) — not `*ngIf`, `*ngFor`
- Bind every form field to `[formControl]` or `formControlName`
- Show validation errors conditionally
- Use exact button labels from Module 3 (not from reference files)
- Every `<img>` has `alt`; every icon button has `aria-label`
- No hardcoded colours or pixel sizes — use CSS classes or Material theme

Template structure:
```html
<div class="[screen-name]-container">
  <!-- Section header -->
  <h1 class="screen-title">[Screen Title from Figma]</h1>

  <!-- Main form -->
  <form [formGroup]="form" (ngSubmit)="onSubmit()">

    <!-- Each section -->
    <section class="[section-name]-section">
      <h2 class="section-title">[Section Title]</h2>

      <!-- Each field — Angular Material example -->
      <mat-form-field appearance="outline">
        <mat-label>[Figma Label]</mat-label>
        <input matInput [formControl]="form.controls.[fieldName]"
               [attr.aria-label]="'[Figma Label]'" />
        @if (form.controls.[fieldName].hasError('required')) {
          <mat-error>[Field label] is required</mat-error>
        }
      </mat-form-field>

    </section>

    <!-- Form footer with buttons -->
    <div class="form-actions">
      <button mat-button type="button" (click)="onCancel()">[Cancel Label]</button>
      <button mat-raised-button color="primary" type="submit"
              [disabled]="isLoading">[Submit Label]</button>
    </div>

  </form>

  <!-- Error display -->
  @if (errorMessage) {
    <div role="alert" class="error-banner">{{ errorMessage }}</div>
  }
</div>
```

### Step 3 — Generate `.component.scss`

```scss
.{screen-name}-container {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
}

.screen-title {
  margin-bottom: 24px;
}

.section-title {
  margin-bottom: 16px;
}

mat-form-field {
  width: 100%;
  margin-bottom: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.error-banner {
  color: var(--mat-sys-error, #b00020);
  margin-top: 16px;
}
```

---

## Output Contract

Returns paths of all created files:
- `[path]/[screen-name].component.ts`
- `[path]/[screen-name].component.html`
- `[path]/[screen-name].component.scss`

---

## Error Handling

- If approved spec is missing a section: note gap, use placeholder with `// TODO` comment
- If Angular version is uncertain: generate standalone component (safe default for Angular 17+)
- If UI library is unspecified: use plain HTML with SCSS (no Material/PrimeNG imports)
