# Module 10: i18n Translations

## Purpose
Add internationalization (i18n) translation keys for all user-facing strings in the new screen. i18n may be **optional** depending on Module 0 project configuration.

---

## Step 10.0: Determine i18n Mode

**Based on Module 0 configuration:**

| Mode | Condition | Action |
|------|-----------|--------|
| **Full i18n** | Module 0: i18n = YES, library configured | Complete all steps 10.1–10.5 |
| **Hackathon mode** | Module 0: i18n = NO or "not now" | Do Step 10.1 (catalog strings) + Step 10.6 (future-proof markers) only; skip Steps 10.2–10.5 |
| **English-only** | Module 0: i18n = single-language | Add keys to en.json only; skip other language files |

**If mode is "Hackathon mode"**, skip to Step 10.6 after Step 10.1.

---

## Process

### Step 10.1: Catalog All User-Facing Strings

Collect ALL strings requiring translation from previous modules:

**From Module 6 (component):**
- Page title (hardcoded in component)
- Section headings in template
- Form field labels
- Placeholder text
- Validation error messages
- Button labels (already confirmed in Module 3)
- Loading/empty state messages

**From Module 9 (success page):**
- Success title
- Success message (with placeholder tokens for dynamic values)
- Success page button labels
- Delete confirmation message

**Document as a table:**
```
KEY                              | English Value                    | Source
---------------------------------|----------------------------------|----------
[SCREEN_NAME]_PAGE_TITLE         | "[exact string]"                 | Module 6
[SCREEN_NAME]_SECTION_1_TITLE    | "[exact string]"                 | Module 6
[SCREEN_NAME]_FIELD_LABEL        | "[exact string from Figma]"      | Module 1
[SCREEN_NAME]_FIELD_REQUIRED     | "[field] is required"            | Module 6
[SCREEN_NAME]_SUBMIT_BTN         | "[exact label from Module 3]"    | Module 3
[SCREEN_NAME]_CANCEL_BTN         | "[exact label from Module 3]"    | Module 3
[SCREEN_NAME]_SUCCESS_TITLE      | "[exact string from Figma]"      | Module 9
[SCREEN_NAME]_SUCCESS_MSG        | "[exact string from Figma]"      | Module 9
```

**Naming convention:**
- Prefix: `[SCREEN_NAME]_` (e.g. `CREATE_USER_`, `ORDER_DETAIL_`)
- Suffix:
  - `_TITLE` — page or section title
  - `_LABEL` — form field label
  - `_PLACEHOLDER` — input placeholder
  - `_REQUIRED` — required validation error
  - `_MAXLENGTH` — max length validation error
  - `_BTN` — button label
  - `_SUCCESS_TITLE` / `_SUCCESS_MSG` — success page
  - `_ARIA` — ARIA label (screen-reader only)

---

### Step 10.2: Identify i18n Library and Config

**Confirm from Module 0:**
- Library in use: `@ngx-translate/core` / Angular `$localize` / `transloco` / none
- Config file format: JSON / TS / PO
- Translation file location(s): (e.g. `src/assets/i18n/`, `public/i18n/`)
- Languages enabled: [en, es, fr, ...] or just [en]

**If no i18n library configured but Module 0 says i18n = YES:**
```
🛑 i18n library is enabled in project config but no translation library is configured.
Which library should I set up?
A) @ngx-translate/core — JSON-based, most common in Angular Material apps
B) Transloco — newer, supports lazy loading
C) Angular built-in $localize — compile-time, CLI-integrated
D) Skip — leave strings inline (hackathon mode)
```

---

### Step 10.3: Add Keys to English Translation File

Locate `en.json` (or equivalent primary language file).

Add the new screen's keys under a grouped block:
```json
{
  "[SCREEN_NAME]_PAGE_TITLE": "[English value]",
  "[SCREEN_NAME]_SECTION_1_TITLE": "[English value]",
  "[SCREEN_NAME]_FIELD_FIRST_NAME_LABEL": "First Name",
  "[SCREEN_NAME]_FIELD_FIRST_NAME_REQUIRED": "First Name is required",
  "[SCREEN_NAME]_FIELD_FIRST_NAME_MAXLENGTH": "First Name cannot exceed 100 characters",
  "[SCREEN_NAME]_SUBMIT_BTN": "[EXACT label from Module 3]",
  "[SCREEN_NAME]_CANCEL_BTN": "[EXACT label from Module 3]",
  "[SCREEN_NAME]_SUCCESS_TITLE": "[EXACT from Figma]",
  "[SCREEN_NAME]_SUCCESS_MSG": "[EXACT from Figma — use {{id}} for dynamic values]"
}
```

**Rules:**
- Flat key structure (no nested objects) unless project already uses nesting — match existing convention
- Dynamic values use `{{paramName}}` tokens: `"Record {{id}} saved successfully"`
- Do NOT alter existing keys — only add new ones

---

### Step 10.4: Add Keys to All Other Language Files

**For each additional language configured in Module 0:**
- Duplicate the key block from Step 10.3
- Set value to the English text with a `[TRANSLATE: <lang>]` prefix marker
- Explain to user: "The non-English keys are marked `[TRANSLATE: es]` — provide translations or use a translation service to complete them"

```json
// es.json (Spanish — example)
{
  "[SCREEN_NAME]_PAGE_TITLE": "[TRANSLATE: es] [English value]",
  "[SCREEN_NAME]_SUBMIT_BTN": "[TRANSLATE: es] [EXACT English label]",
  ...
}
```

**If project has a translation service or approved translations already:**
Ask user: "Do you have the translated strings for [language list], or should I mark them for translation?"

---

### Step 10.5: Update Component to Use i18n Keys

**For @ngx-translate/core pattern:**
```typescript
// In component — inject TranslateService:
private readonly translate = inject(TranslateService);

// Dynamic title in component (optional — prefer template pipe):
// protected pageTitle = this.translate.instant('[SCREEN_NAME]_PAGE_TITLE');
```

```html
<!-- In template: -->
<h1>{{ '[SCREEN_NAME]_PAGE_TITLE' | translate }}</h1>
<mat-label>{{ '[SCREEN_NAME]_FIELD_FIRST_NAME_LABEL' | translate }}</mat-label>

<!-- Dynamic values: -->
<p>{{ '[SCREEN_NAME]_SUCCESS_MSG' | translate: { id: successData.id } }}</p>
```

**For Angular $localize pattern:**
```typescript
protected pageTitle = $localize`[Screen Page Title]`;
```

**For Transloco pattern:**
```html
<h1>{{ '[SCREEN_NAME]_PAGE_TITLE' | transloco }}</h1>
```

**ARIA labels with i18n:**
```html
<input [attr.aria-label]="'[SCREEN_NAME]_FIELD_LABEL_ARIA' | translate" />
```

---

### Step 10.6: Hackathon Mode — Future-Proof Inline Strings

When i18n is skipped (Module 0: i18n = NO), mark all strings for easy future extraction:

```typescript
// In component — add a comment block:
// i18n-TODO: extract the following strings when i18n is added
// '[SCREEN_NAME]_PAGE_TITLE' = '[English value]'
// '[SCREEN_NAME]_SUBMIT_BTN' = '[English value]'
// '[SCREEN_NAME]_SUCCESS_MSG' = '[English value]'

protected readonly pageTitle = '[English value]';  // i18n-TODO: [SCREEN_NAME]_PAGE_TITLE
```

```html
<!-- In template: mark inline strings for future extraction -->
<!-- i18n: [SCREEN_NAME]_SUBMIT_BTN -->
<button>[EXACT English label from Module 3]</button>
```

This makes grep-based i18n extraction straightforward when i18n is added later.

---

### Step 10.7: Run WCAG Accessibility Skill

**After i18n keys are in place, invoke the WCAG Accessibility skill:**

```
Invoke: .github/skills/wcag-accessibility/SKILL.md
```

Pass:
- Component HTML file path
- Screen name (for ARIA key naming)
- i18n mode (to determine if ARIA labels use translation keys or inline text)

The skill checks all 10 WCAG 2.1 AA rules and produces a results report. Address any AUTO-FIX items before the Module 10 completion report.

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report before proceeding to Module 11.**

```
✅ MODULE 10 COMPLETION REPORT: i18n Translations

1. i18n Mode:
   - Mode: [Full i18n / Hackathon mode / English-only]
   - Library: [@ngx-translate/core / Transloco / $localize / none]

2. Keys Catalogued:
   - Total unique keys: [N]
   - Key prefix used: [SCREEN_NAME_]

3. Translation Files Updated:
   - en.json: [YES — N keys added / SKIPPED]
   - es.json: [YES — N keys added / SKIPPED / MARKED for translation]
   - fr.json: [YES / SKIPPED / MARKED]
   - [other]: [YES / SKIPPED / MARKED]

4. Component Updated to Use Keys:
   - Pipe/translate used in template: [YES / NO — hackathon inline]
   - ARIA labels use translation keys: [YES / NO — hackathon inline]
   - Dynamic value tokens documented: [YES / N-A]

5. Hackathon Mode Markers:
   - i18n-TODO comments added: [YES / N-A]
   - Number of marked strings: [N or "N-A"]

6. WCAG Accessibility Skill:
   - Invoked: [YES / NO — reason if skipped]
   - Issues found: [N or "none"]
   - Auto-fixes applied: [N or "none"]
   - Remaining manual actions: [list or "none"]

✅ Module 10 Status: [COMPLETE/INCOMPLETE]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 11
```

**DO NOT proceed to Module 11 until user confirms this report.**

---

## Next Module
Proceed to **Module 11: Testing**.
