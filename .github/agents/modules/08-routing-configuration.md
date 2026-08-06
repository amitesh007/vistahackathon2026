# Module 8: Routing Configuration

## Purpose
Register the new screen in Angular Router and document navigation patterns for reaching it.

## Inputs
- Component implementation (from Module 6)
- Route path confirmed in Module 0
- Whether edit mode needs a route param (e.g. `:id`)
- Whether any route guard is required (auth, unsaved-changes)

---

## CRITICAL: Standard Angular Routing (No Proprietary Patterns)

This module configures **standard Angular Router** only:
- Route registered in `app.routes.ts` (or the routes file confirmed in Module 0)
- Lazy-loaded via `loadComponent` (for standalone) or `loadChildren` (for NgModule)
- Route params used only when the screen needs them (e.g. edit mode with `:id`)
- **No timestamp-based routing, no SharedLibraryService, no tab management** — those are LoanIQ-specific patterns not needed here.

---

## Process

### Step 8.1: Determine Route Configuration

Based on Module 0 and Module 5 spec:

**Simple routed page (most common for hackathon):**
```typescript
// src/app/app.routes.ts (or confirmed routes file)
export const routes: Routes = [
  // ...existing routes

  {
    path: '[confirmed-route-path]',           // e.g. 'create-user'
    loadComponent: () => import('./features/[screen-name]/[screen-name].component')
      .then(m => m.[ScreenName]Component)
  },
];
```

**Routed page with edit param:**
```typescript
{
  path: '[confirmed-route-path]',           // e.g. 'users/create'
  loadComponent: () => import('./features/[screen-name]/[screen-name].component')
    .then(m => m.[ScreenName]Component)
},
{
  path: '[confirmed-route-path]/:id',       // e.g. 'users/:id/edit'
  loadComponent: () => import('./features/[screen-name]/[screen-name].component')
    .then(m => m.[ScreenName]Component)
},
```

**Dialog/modal (if screen confirmed as dialog in Module 5):**
- Do NOT register as a route — dialogs are opened programmatically via `MatDialog.open()`
- Document the caller's invocation pattern instead (see Step 8.3)

**Nested under parent route:**
```typescript
{
  path: '[parent]',
  children: [
    {
      path: '[child-route]',
      loadComponent: () => import('./features/[screen-name]/[screen-name].component')
        .then(m => m.[ScreenName]Component)
    }
  ]
}
```

### Step 8.2: Confirm Route Guard (if applicable)

Ask user if needed:
```
Does this route require:
A) Authentication guard (user must be logged in) — if yes, which guard name?
B) Unsaved-changes guard (warns before leaving with unsaved form) — should I implement canDeactivate?
C) No guard needed
```

**If auth guard confirmed:**
```typescript
{
  path: '[route-path]',
  canActivate: [authGuard],    // functional guard name from project
  loadComponent: () => ...
}
```

**If unsaved-changes guard confirmed:**
```typescript
// src/app/core/guards/unsaved-changes.guard.ts
export const unsavedChangesGuard: CanDeactivateFn<{ hasUnsavedChanges: () => boolean }> =
  (component) => {
    if (component.hasUnsavedChanges()) {
      return confirm('You have unsaved changes. Leave anyway?');
    }
    return true;
  };

// Add to component:
// hasUnsavedChanges(): boolean { return this.form.dirty; }
```

```typescript
// In routes:
{
  path: '[route-path]',
  canDeactivate: [unsavedChangesGuard],
  loadComponent: () => ...
}
```

### Step 8.3: Document Navigation Pattern

Document how parent screens navigate TO this screen:

**For a standard routed page:**
```typescript
// In the parent component that opens this screen:
import { Router } from '@angular/router';

private readonly router = inject(Router);

// Create mode (no ID needed):
navigateToCreate(): void {
  this.router.navigate(['/[route-path]']);
}

// Edit mode (pass ID as route param):
navigateToEdit(id: string): void {
  this.router.navigate(['/[route-path]', id]);
}
```

**For a dialog-based screen:**
```typescript
// In the parent component that opens this dialog:
import { MatDialog } from '@angular/material/dialog';
import { [ScreenName]Component } from './features/[screen-name]/[screen-name].component';

private readonly dialog = inject(MatDialog);

openDialog(entityId?: string): void {
  const dialogRef = this.dialog.open([ScreenName]Component, {
    width: '600px',
    data: { entityId }  // passed as @Input or via MAT_DIALOG_DATA
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result) {
      // handle saved result
    }
  });
}
```

### Step 8.4: Add Component ActivatedRoute Subscription (if route params used)

Verify Module 6 component reads route params:
```typescript
// In component ngOnInit():
const id = this.route.snapshot.paramMap.get('id');
if (id) {
  this.loadExistingData(id);
}
```

If component does NOT read the param but the route has `:id`, add the subscription now.

### Step 8.5: Verify RouterModule / Router Provider

For standalone apps (Angular 17+), ensure `provideRouter` is in `app.config.ts`:
```typescript
// src/app/app.config.ts
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // ...
  ]
};
```

For NgModule apps: `RouterModule.forRoot(routes)` in `AppModule.imports`. Confirm this already exists — do NOT add it again.

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report before proceeding to Module 9.**

```
✅ MODULE 8 COMPLETION REPORT: Routing Configuration

1. Route Registered:
   - Routes file modified: [exact path]
   - Route path: [/full/path] or [/parent/child-path]
   - Route type: [standalone loadComponent / loadChildren / dialog — no route]
   - Edit param: [/:id / none]

2. Route Guard:
   - Auth guard: [YES — guard name / NO]
   - Unsaved-changes guard: [YES / NO]
   - Other guards: [list or "none"]

3. Navigation Pattern Documented:
   - Create mode: [YES — method shown / N-A]
   - Edit mode: [YES — method shown / N-A]
   - Dialog open: [YES — method shown / N-A]

4. Component Route Param Handling:
   - Component reads :id param: [YES / NO / N-A]

5. Router Provider:
   - provideRouter / RouterModule confirmed present: [YES]

6. Files Modified:
   - [exact path to routes file]
   - [exact path to guard file if new]

✅ Module 8 Status: [COMPLETE/INCOMPLETE]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 9
```

**DO NOT proceed to Module 9 until user confirms this report.**

---

## Next Module
Proceed to **Module 9: Success / Result Page Configuration**.
