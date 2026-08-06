# Module 6: Angular Component Implementation

## Purpose
Create the Angular component files (`.ts` + `.html` + `.scss`) from the approved Component Specification (Module 5).

## Load Instruction File
**Load now:** `.github/instructions/angular-component-standards.instructions.md`

## Inputs
- Approved Component Design Spec (from Module 5)
- Project context from Module 0 (Angular version, UI library, standalone/module-based)
- Exact button labels (from Module 3)
- Icon decisions (from Module 4)

## STOP Checkpoint
**Before creating files, confirm:**
- Component spec is APPROVED by user (Module 5 STOP Gate 2 passed)
- Angular version confirmed (determines standalone vs NgModule approach)
- UI library confirmed (determines which component imports to add)
- Feature folder path confirmed (from Module 0 project structure)

---

## Process

### Step 6.1: Create Feature Folder and Files

Create at the path confirmed in Module 0:
```
[features-path]/[screen-name]/
├── [screen-name].component.ts
├── [screen-name].component.html
└── [screen-name].component.scss
```

### Step 6.2: Generate Component TypeScript File

Follow patterns from `.github/instructions/angular-component-standards.instructions.md`.

**Structure (in order):**
```typescript
// 1. Framework imports
import { Component, OnInit, OnDestroy, inject, DestroyRef, Input, Output, EventEmitter } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
// 2. UI library imports (only what's used in template)
// Angular Material example:
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
// PrimeNG alternative or plain — match project library
// 3. Application imports
import { [ScreenName]Service } from '../../core/services/[screen-name].service';
import { [EntityName], Create[EntityName]Request } from './models/[entity-name].model';

@Component({
  selector: 'app-[screen-name]',
  standalone: true,         // if standalone confirmed; omit for NgModule
  imports: [
    ReactiveFormsModule,
    // ONLY what is used in the template:
    MatFormFieldModule, MatInputModule, MatButtonModule, // ... etc.
  ],
  templateUrl: './[screen-name].component.html',
  styleUrl: './[screen-name].component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush  // if possible
})
export class [ScreenName]Component implements OnInit {

  // ─── Injected services ───────────────────────────────────────────────────
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);           // only if using route params
  private readonly [screenName]Service = inject([ScreenName]Service);
  private readonly destroyRef = inject(DestroyRef);

  // ─── Inputs / Outputs ────────────────────────────────────────────────────
  // (from spec — include only if this is a child component / dialog)
  @Input() entityId?: string;
  @Output() saved = new EventEmitter<[EntityName]>();
  @Output() cancelled = new EventEmitter<void>();

  // ─── Reactive form ───────────────────────────────────────────────────────
  // (from spec — omit for non-form screens)
  protected readonly form = new FormGroup({
    // For each field in approved spec:
    [fieldName]: new FormControl<[type]>([default], {
      nonNullable: true,
      validators: [Validators.required, ...]  // per spec
    }),
  });

  // ─── State ───────────────────────────────────────────────────────────────
  protected isLoading = false;
  protected errorMessage = '';
  protected pageTitle = '[EXACT SCREEN TITLE FROM FIGMA]';

  // ─── Edit mode data ──────────────────────────────────────────────────────
  // (only if edit mode is supported)
  protected existingData: [EntityName] | null = null;

  // ─── Lifecycle ───────────────────────────────────────────────────────────
  ngOnInit(): void {
    // Read route param for edit mode (if applicable)
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadExistingData(id);
    }
  }

  // ─── Event handlers (use exact names from spec) ──────────────────────────
  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    const payload: Create[EntityName]Request = this.form.getRawValue();

    this.[screenName]Service.create(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.router.navigate(['/[success-or-list-route]']);
        },
        error: (err: Error) => {
          this.errorMessage = err.message;
          this.isLoading = false;
        }
      });
  }

  protected onCancel(): void {
    this.router.navigate(['/[cancel-route]']);
    // OR: this.cancelled.emit() if dialog component
  }

  // ─── Private helpers ─────────────────────────────────────────────────────
  private loadExistingData(id: string): void {
    this.isLoading = true;
    this.[screenName]Service.getById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.existingData = data;
          this.form.patchValue(data as any);
          this.isLoading = false;
        },
        error: (err: Error) => {
          this.errorMessage = err.message;
          this.isLoading = false;
        }
      });
  }
}
```

**CRITICAL RULES for TypeScript:**
- Use `inject()` function, NOT constructor injection
- Access modifiers: `private readonly` for services; `protected` for template-bound properties
- No `any` types unless unavoidable — use typed interfaces from Module 5
- `takeUntilDestroyed(this.destroyRef)` on all subscriptions
- `form.markAllAsTouched()` before checking validity on submit
- No `console.log` in production code

### Step 6.3: Generate Component HTML Template

Follow `angular-component-standards.instructions.md` patterns.

**Rules:**
- Use `@if` / `@for` / `@switch` (Angular 17+ control flow) — NOT `*ngIf` / `*ngFor`
- Use `async` pipe where possible instead of manual subscriptions
- Every form field bound to `[formControl]` or `formControlName`
- Validation errors displayed with `@if (control.hasError('required') && control.touched)`
- EXACT button labels from Module 3 (NOT from reference files, NOT assumed)
- Icons apply `aria-hidden="true"` if decorative; `aria-label="[action]"` if interactive

**Template skeleton:**
```html
<div class="[screen-name]-container">

  <!-- Page header -->
  <header class="page-header">
    <h1 class="page-title">{{ pageTitle }}</h1>
    <!-- Toolbar icon actions (from Module 4 — only if found in Figma) -->
  </header>

  <!-- Error banner (global async error) -->
  @if (errorMessage) {
    <div role="alert" aria-live="assertive" class="error-banner">
      {{ errorMessage }}
    </div>
  }

  <!-- Loading overlay (if needed) -->
  @if (isLoading) {
    <div class="loading-overlay" role="status" aria-label="Loading">
      <!-- mat-spinner or project-specific spinner -->
    </div>
  }

  <!-- Main form (if form screen) -->
  <form [formGroup]="form" (ngSubmit)="onSubmit()" novalidate>

    <!-- Section 1: [Name from Figma] -->
    <section class="[section-name]-section" aria-labelledby="section1-heading">
      <h2 id="section1-heading" class="section-title">[Section Title — EXACT from Figma]</h2>

      <!-- For each field from Module 1 spec: -->
      <mat-form-field appearance="outline">
        <mat-label>[EXACT Figma Label]</mat-label>
        <input matInput
               formControlName="[fieldName]"
               [attr.aria-label]="'[EXACT Figma Label]'"
               placeholder="[placeholder from Figma or empty]" />
        @if (form.controls.[fieldName].hasError('required') && form.controls.[fieldName].touched) {
          <mat-error>[EXACT Figma Label] is required</mat-error>
        }
        @if (form.controls.[fieldName].hasError('maxlength')) {
          <mat-error>[EXACT Figma Label] cannot exceed [N] characters</mat-error>
        }
      </mat-form-field>

      <!-- Repeat for all fields in this section -->

    </section>

    <!-- Section 2: [Name from Figma] — repeat pattern -->

    <!-- Form footer with action buttons -->
    <footer class="form-actions">
      <!-- Secondary buttons (left / text buttons) — EXACT labels from Module 3 -->
      <button mat-button type="button" (click)="onCancel()">
        [EXACT Cancel/Discard label from Module 3]
      </button>
      <!-- Primary button (right / filled) — EXACT label from Module 3 -->
      <button mat-raised-button color="primary" type="submit"
              [disabled]="isLoading">
        @if (isLoading) { Saving... }
        @else { [EXACT Submit label from Module 3] }
      </button>
    </footer>

  </form>

  <!-- Non-form content (if table/list screen) -->
  <!-- Insert table, card list, or data display here using same Figma-exact column headers -->

</div>
```

### Step 6.4: Generate SCSS File

```scss
.{screen-name}-container {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 16px;
}

.[section-name]-section {
  margin-bottom: 32px;
}

mat-form-field {
  width: 100%;
  margin-bottom: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--mat-sys-outline-variant, #e0e0e0);
}

.error-banner {
  color: var(--mat-sys-error, #b00020);
  background: var(--mat-sys-error-container, #fdecea);
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.loading-overlay {
  display: flex;
  justify-content: center;
  padding: 32px;
}
```

### Step 6.5: Identify All Hardcoded Strings

Search component for ALL user-facing strings that may need i18n:
- `pageTitle = '[Screen Name]'` → i18n key candidate
- Error messages: `this.errorMessage = '...'` → i18n key candidate
- Button labels in template → should already use Figma-exact text; i18n key candidates
- Section titles in template → i18n key candidates

**Document ALL identified strings for Module 10 (i18n).**

---

## STOP Gate 3 — Before Module 8

```
Before proceeding to Module 8 (Routing):
□ Component .ts file created at confirmed path
□ Component .html file created (renders without structural errors)
□ Component .scss file created
□ All form fields from spec implemented
□ All buttons implemented with EXACT labels from Module 3
□ All icons implemented per Module 4 decisions
□ No console.log left in code
□ Hardcoded strings identified for Module 10

ALL must be YES to proceed.
```

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report before proceeding to Module 7.**

```
✅ MODULE 6 COMPLETION REPORT: Component Implementation

1. Files Created:
   - [path]/[screen-name].component.ts → [N] lines
   - [path]/[screen-name].component.html → [N] lines
   - [path]/[screen-name].component.scss → [N] lines

2. Form Implementation (if form screen):
   - FormGroup: [name]
   - FormControls implemented: [N] — matches spec: [CONFIRMED]
   - Validators applied: [list]
   - Submit handler: [YES/NO]
   - Cancel handler: [YES/NO]

3. Button Implementation:
   - Total buttons: [N]
   - Labels verified against Module 3: [CONFIRMED]
   - Disabled states implemented: [YES/NO]

4. Icon Implementation (from Module 4):
   - Interactive icons: [N] — with aria-label: [CONFIRMED]
   - Decorative icons: [N] — with aria-hidden="true": [CONFIRMED]

5. Template Features:
   - Angular control flow (@if/@for): [CONFIRMED / legacy *ngIf used — reason:]
   - Validation error messages: [N fields]
   - Loading state: [IMPLEMENTED]
   - Error banner: [IMPLEMENTED]

6. Dependency Injection:
   - Using inject() function: [CONFIRMED]
   - No constructor injection: [CONFIRMED]
   - takeUntilDestroyed used: [CONFIRMED]
   - No console.log: [CONFIRMED]

7. Hardcoded Strings for i18n:
   - Total strings identified: [N]
   - List: [all strings documented for Module 10]

8. STOP Gate 3 Results:
   - All checklist items PASS: [YES / NO — list failures]

✅ Module 6 Status: [COMPLETE/INCOMPLETE]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 7
```

**DO NOT proceed to Module 7 until user confirms this report.**

---

## Next Module
Proceed to **Module 7: Angular Service Implementation**.
