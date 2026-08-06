# Module 9: Success / Result Page Configuration

## Purpose
Implement the success or confirmation state shown after the user completes the screen's primary action (create, update, delete). The success content MUST come from Figma — never from assumptions or reference files.

## MANDATORY STOP GATE

**Before implementing this module, confirm:**
1. Did user confirm a success/result page is needed? [Yes/No/N-A]
2. Did you fetch Figma design context for the success state? [Yes/No/N-A]
3. Did you extract the success title from Figma? [Yes/No/N-A]
4. Did you extract the success message from Figma? [Yes/No/N-A]
5. Did you extract button labels from the success page Figma? [Yes/No/N-A]

**If ANY answer is "No" (and a success page was confirmed), STOP and fetch the Success Page Figma first.**
**If user confirmed NO success page needed → mark this module as N/A and skip to Module 10.**

---

## CRITICAL: Success Content Must Come From Figma

**If Success Page Figma URL not yet provided:**
```
🛑 I need the Success Page Figma URL to extract the exact success message text.
Could you provide the Figma URL for the success/confirmation screen?
```

**If success message text is unclear in Figma context:**
```
🛑 The success message text in the Figma context is not clear.
Could you confirm the exact text that should appear after a successful [create/update/delete]?
```

---

## Process

### Step 9.1: Fetch Success Page Figma

Call `mcp__figma__get_design_context` for the Success Page node ID.

Extract:
- Success icon/illustration (name or visual description)
- Success title text (exact)
- Success message text (exact, note any dynamic values like record ID or name)
- Primary button label (exact)
- Secondary button label (exact, if present)
- Any additional context fields shown (transaction reference, summary of what was done)

**Document:**
```
Success Page Extraction:
- Title: "[EXACT TEXT from Figma]"
- Message: "[EXACT TEXT — note {id} or {name} placeholders if present]"
- Primary Button: "[EXACT TEXT]"
- Secondary Button: "[EXACT TEXT or N/A]"
- Additional fields: [list or "none"]
```

### Step 9.2: Determine Success State Type

Based on Module 5 spec and Figma success page design:

**Option A — Inline success state** (same component, toggle a flag):
- Best for: short forms, dialogs, simple confirmations
- Implementation: add `isSuccess = false` flag; after successful submit, set `isSuccess = true`; use `@if` in template to switch between form view and success view

**Option B — Separate routed success page** (navigate to `/[screen]/success`):
- Best for: multi-step wizards, complex transactions
- Implementation: create a new `[screen-name]-success.component.ts` with its own route

**Option C — Dialog/toast notification** (brief confirmation message):
- Best for: table row actions (edit/delete), quick operations
- Implementation: `MatSnackBar.open('[success message]', 'OK', { duration: 3000 })`

**Ask user if unclear:**
```
How should the success state be shown?
A) Inline — same page switches to a success view (no navigation)
B) Separate route — navigate to /[path]/success
C) Toast/snackbar — brief overlay message
```

### Step 9.3: Implement the Chosen Pattern

**Option A — Inline success state:**
```typescript
// In component:
protected isSuccess = false;
protected successData: { id: string } | null = null;

protected onSubmit(): void {
  // ... form validation ...
  this.service.create(payload)
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: (result) => {
        this.successData = { id: result.id };
        this.isSuccess = true;
      },
      error: (err) => { this.errorMessage = err.message; }
    });
}

protected onSuccessPrimaryAction(): void {
  // "[Primary Button label from Figma]" action
  this.router.navigate(['/[list-or-view-route]']);
}

protected onSuccessSecondaryAction(): void {
  // "[Secondary Button label from Figma]" action (if present)
  this.router.navigate(['/[home-or-dashboard-route]']);
}
```

```html
<!-- In component template, wrap main content: -->
@if (!isSuccess) {
  <!-- form / main content -->
}
@if (isSuccess) {
  <div class="success-container" role="status">
    <!-- Success icon (from Figma) -->
    <h2 class="success-title">[EXACT title from Figma]</h2>
    <p class="success-message">
      [EXACT message from Figma]
      @if (successData) { — ID: {{ successData.id }} }
    </p>
    <div class="success-actions">
      <button mat-raised-button color="primary" (click)="onSuccessPrimaryAction()">
        [EXACT primary button label from Figma]
      </button>
      <!-- Only if secondary button exists in Figma: -->
      <button mat-button (click)="onSuccessSecondaryAction()">
        [EXACT secondary button label from Figma]
      </button>
    </div>
  </div>
}
```

**Option B — Separate success route:**
```typescript
// In main component after successful submit:
this.router.navigate(['/[screen-name]/success'], {
  state: { entityId: result.id, entityName: result.name }
});
```

```typescript
// Create [screen-name]-success.component.ts:
@Component({
  selector: 'app-[screen-name]-success',
  standalone: true,
  imports: [MatButtonModule],
  template: `
    <div class="success-container" role="status">
      <h2>[EXACT title from Figma]</h2>
      <p>[EXACT message from Figma]</p>
      <button mat-raised-button color="primary" (click)="onPrimary()">
        [EXACT primary button label from Figma]
      </button>
    </div>
  `
})
export class [ScreenName]SuccessComponent {
  private router = inject(Router);

  protected onPrimary(): void {
    this.router.navigate(['/[list-route]']);
  }
}
```

**Add route for success page:**
```typescript
{ path: '[screen-name]/success', component: [ScreenName]SuccessComponent }
```

**Option C — Toast/snackbar:**
```typescript
// Inject in component:
private readonly snackBar = inject(MatSnackBar);

// After successful submit:
this.snackBar.open(
  '[EXACT success message from Figma]',
  '[EXACT button label from Figma]',
  { duration: 4000 }
);
// Then navigate or refresh list
```

### Step 9.4: Delete Confirmation Pattern

If the screen has a delete action:
```typescript
protected onDelete(entityId: string): void {
  const confirmed = confirm('[EXACT confirmation message — or from dialog]');
  if (!confirmed) return;

  this.service.delete(entityId)
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: () => {
        // Show success per chosen pattern above
        this.router.navigate(['/[list-route]']);
      },
      error: (err) => { this.errorMessage = err.message; }
    });
}
```

For nicer UX, use `MatDialog` for a confirmation dialog instead of `confirm()`.

### Step 9.5: Document All Success-Related Strings for Module 10

List all strings that will need i18n keys:
- Success title
- Success message (with `{{id}}` or `{{name}}` placeholders)
- Primary button label
- Secondary button label
- Delete confirmation message

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report before proceeding to Module 10.**

```
✅ MODULE 9 COMPLETION REPORT: Success Page Configuration

Status: [COMPLETE / SKIPPED — no success page required]

1. Success Page Figma:
   - Figma URL: [URL or "N/A"]
   - Title extracted: "[EXACT TEXT or N/A]"
   - Message extracted: "[EXACT TEXT or N/A]"
   - Primary button: "[EXACT TEXT or N/A]"
   - Secondary button: "[EXACT TEXT or N/A]"

2. Implementation Type:
   - Pattern chosen: [inline flag / separate route / toast / N/A]
   - Reason: [why this pattern suits the design]

3. Files Created/Modified:
   - [list or "none"]

4. Action Routes:
   - Primary button → [/path or action]
   - Secondary button → [/path or N/A]

5. Delete Confirmation:
   - Delete handler implemented: [YES/NO/N-A]
   - Confirmation method: [confirm() / MatDialog / N/A]

6. Strings for Module 10:
   - Success title key: [proposed key or "N/A"]
   - Success message key: [proposed key or "N/A"]
   - Button label keys: [list or "N/A"]

✅ Module 9 Status: [COMPLETE / N/A]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 10
```

**DO NOT proceed to Module 10 until user confirms this report.**

---

## Next Module
Proceed to **Module 10: i18n Translations**.
