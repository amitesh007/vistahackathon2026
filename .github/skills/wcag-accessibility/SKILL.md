# Skill: wcag-accessibility

**Version**: 1.0.0
**Invoked by**: `angular-ui` agent — Module 10a (MANDATORY)
**Purpose**: Audit all new Angular component HTML templates for WCAG 2.1 Level AA compliance and apply safe auto-fixes.

---

## When Invoked

Invoked at Module 10a, after i18n (Module 10). MANDATORY for every new screen — cannot be skipped.

---

## Input

- Path to `[screen-name].component.html` (primary audit target)
- Path to any dialog/modal templates introduced by this screen
- Path to success page template (if Module 9 was implemented)
- Current i18n mode: enabled (translate pipe) / disabled (inline English)

---

## Audit Checklist

Run each check. Mark: ✅ PASS / ❌ FAIL / ⚠️ AUTO-FIXED / 🔧 MANUAL FIX REQUIRED

---

### A1 — Images and Icons

- Every `<img>` has `alt` attribute.
  - Decorative `<img>`: `alt=""`
  - Informative `<img>`: `alt="[meaningful description]"`
- Every `<mat-icon>` or `<span class="icon">` that is **decorative** has `aria-hidden="true"`.
- Every `<mat-icon>` that is **interactive** (inside a button) has an accessible label.

Auto-fix: Add `aria-hidden="true"` to decorative icons inside buttons where a visible text label also exists.

---

### A2 — Form Field Labels

- Every `<input>`, `<select>`, `<textarea>` has a visible `<label>` or is inside `<mat-form-field>` with `<mat-label>`.
- Every input has an `aria-label` or `aria-labelledby` attribute when no visible label is present.
- No input relies on `placeholder` alone as its label (placeholder is not a label).

Auto-fix: Add `[attr.aria-label]="'[field label text]'"` to inputs missing accessible names.

---

### A3 — Icon-Only Buttons

- Every `<button>` containing only an icon (no visible text) has `aria-label="[action description]"`.
- Examples: close buttons, delete icons, edit pencil icons.

Auto-fix: Add `aria-label="[derived from icon name/context]"` if clearly derivable.

---

### A4 — Error Messages

- All validation errors use `<mat-error>` (inside `<mat-form-field>`) or `role="alert"` / `aria-live="polite"`.
- Error messages are programmatically associated with the field (via `<mat-error>` or `aria-describedby`).
- Error state MUST NOT rely on colour alone (e.g. red border + icon + text).

---

### A5 — Focus Management

- No `tabindex` value greater than 0 (except intentional focus trapping in dialogs).
- After a dialog closes, focus returns to the element that triggered it.
- After route navigation, page `<h1>` or main content area receives focus.

---

### A6 — Dynamic Content

- Status messages shown after async operations (e.g. "Saved successfully") use `role="alert"` or `aria-live="polite"`.
- Loading spinners/progress bars have `aria-label="Loading"` or equivalent.

---

### A7 — Data Tables

- Every `<table>` has `<th>` with `scope="col"` for column headers.
- Row headers (if any) use `scope="row"`.
- Tables with captions use `<caption>` element.

---

### A8 — Keyboard Navigation

- All interactive elements (buttons, links, form fields, custom controls) are reachable by Tab key.
- Custom interactive elements that are not native HTML inputs use `role` and `tabindex="0"` with keyboard event handlers.
- Dropdown menus, date pickers, and modals trap focus when open and release on close/Escape.

---

### A9 — Colour Contrast (Informational)

Flag (do not auto-fix — requires design review):
- Text on coloured backgrounds should meet 4.5:1 contrast ratio (normal text) or 3:1 (large text / UI components).
- Note any hardcoded colours in SCSS that may violate contrast requirements.

---

### A10 — ARIA Roles

- Custom widget roles (`role="dialog"`, `role="listbox"`, etc.) are used only when appropriate.
- `role="alert"` is used for urgent errors; `aria-live="polite"` for non-urgent updates.
- No redundant ARIA (e.g. `role="button"` on a native `<button>`).

---

## Auto-Fix Actions

The following fixes are applied directly to the `.html` file without user confirmation:

| Issue | Auto-Fix Applied |
|-------|-----------------|
| Decorative `<mat-icon>` inside button with text label | Add `aria-hidden="true"` |
| `<input>` with visible `<mat-label>` but missing `aria-label` | Add `[attr.aria-label]` bound to label text |
| Icon-only button with derivable action from icon name | Add `aria-label` |
| `<img>` with no `alt` attribute | Add `alt=""` (flags for manual review if image appears informative) |

---

## Audit Report

After completing all checks, produce:

```
==============================================
WCAG 2.1 AA Accessibility Audit Report
==============================================
Component: [screen-name].component.html
Date: [timestamp]

A1 — Images & Icons:          [PASS / AUTO-FIXED / FAIL]
A2 — Form Field Labels:        [PASS / AUTO-FIXED / FAIL]
A3 — Icon-Only Buttons:        [PASS / AUTO-FIXED / FAIL]
A4 — Error Messages:           [PASS / FAIL]
A5 — Focus Management:         [PASS / FAIL / N/A]
A6 — Dynamic Content:          [PASS / FAIL / N/A]
A7 — Data Tables:              [PASS / FAIL / N/A]
A8 — Keyboard Navigation:      [PASS / FAIL]
A9 — Colour Contrast:          [INFORMATIONAL — [N] items to review]
A10 — ARIA Roles:              [PASS / FAIL]

Auto-fixes applied: [N]
Manual fixes applied: [N]
----------------------------------------------
Remaining issues: [N]

[If remaining issues > 0:]
BLOCKED — Fix the following before Module 11:
  ❌ [issue description at line N]
==============================================
```

---

## Exit Conditions

- **PASS (0 remaining issues)**: Report to calling agent; proceed to Module 11a.
- **BLOCKED (remaining issues > 0)**: Present issues to user; agent applies manual fixes; re-invoke skill.

---

## i18n-Mode Note

- If i18n is **enabled**: ARIA labels should use translate keys bound via `[attr.aria-label]="'KEY' | translate"`. After adding new ARIA keys, remind the calling agent to add them to all language files.
- If i18n is **disabled** (hackathon mode): ARIA labels use inline English strings. Add `// TODO: i18n` comment.
