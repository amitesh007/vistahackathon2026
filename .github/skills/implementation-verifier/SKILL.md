# Skill: implementation-verifier

**Version**: 1.0.0
**Invoked by**: `angular-ui` agent — Module 11a
**Purpose**: Automated checklist verification that a new Angular screen is correctly wired before Jest spec generation.

---

## When Invoked

Invoked at Module 11a. Must pass (all checks PASS) before the `jest-generator` sub-agent is started.

---

## Verification Checklist (7 Points)

Run each check in sequence. Record PASS or FAIL with details.

---

### Check 1 — Component File Exists

- Confirm that `[screen-name].component.ts` exists at the expected path (confirmed in Module 6).
- Confirm it contains `@Component({ ... })` decorator.
- Confirm selector matches convention: `app-[screen-name]`.

```
✅ PASS: [path] found, @Component present, selector correct
❌ FAIL: [reason — file missing / decorator missing / wrong selector]
```

---

### Check 2 — Component Template Exists

- Confirm `[screen-name].component.html` exists alongside `.ts` file.
- Confirm it is non-empty (more than 5 lines).
- Confirm it contains at least one `<form>` tag (if screen is a form screen) OR at least one data display element.

```
✅ PASS: Template found, non-empty, form/data element present
❌ FAIL: [reason]
```

---

### Check 3 — Service File Exists

- Confirm `[screen-name].service.ts` exists at the expected path (confirmed in Module 7).
- Confirm it has `@Injectable({ providedIn: 'root' })` or is provided in a module/component.
- Confirm it injects `HttpClient` (if API integration was confirmed in Module 0) OR has stub methods (if no API).

```
✅ PASS: Service found, injectable, [API/stub] methods present
❌ FAIL: [reason]
```

---

### Check 4 — Route Registered

- Open the routes file confirmed in Module 0 (e.g. `src/app/app.routes.ts`).
- Search for the confirmed route path from Module 8.
- Confirm the route maps to the correct component (lazy loaded or direct import).

```
✅ PASS: Route '[path]' found in [routes file], maps to [ScreenName]Component
❌ FAIL: Route not found / maps to wrong component
```

---

### Check 5 — Form Controls Match Design (for form screens)

- Count `FormControl` instances in the component `.ts` file.
- Count form fields documented in the Design Spec (from Module 1).
- The counts must match (±1 allowed for hidden/programmatic controls).

```
✅ PASS: [N] FormControls match [N] Figma form fields
❌ FAIL: [N] FormControls vs [M] Figma fields — discrepancy of [diff]
```

If screen is NOT a form (table/list/dashboard), mark this check as N/A.

---

### Check 6 — No console.log in Production Code

- Scan `[screen-name].component.ts` and `[screen-name].service.ts` for `console.log(`, `console.error(`, `console.warn(`.
- Any occurrence is a FAIL (these must be removed before production code).

```
✅ PASS: No console.log/error/warn found
❌ FAIL: Found [N] console.* calls at lines [list]
```

---

### Check 7 — No Blocking TODOs

- Scan all generated files for `// TODO` comments.
- Flag any TODO that blocks functionality (e.g. `// TODO: implement submit`, `// TODO: add route`).
- Non-blocking TODOs (e.g. `// TODO: i18n`) are PASS.

```
✅ PASS: No blocking TODOs found (or only non-blocking TODOs present)
❌ FAIL: [N] blocking TODOs found: [list with line numbers]
```

---

## Verification Report

After all 7 checks, produce:

```
==============================================
Angular UI Implementation Verification Report
==============================================
Screen: [ScreenName]Component
Path: [feature folder path]
Date: [timestamp]

Check 1 — Component File:        [PASS / FAIL]
Check 2 — Component Template:    [PASS / FAIL]
Check 3 — Service File:          [PASS / FAIL]
Check 4 — Route Registered:      [PASS / FAIL]
Check 5 — Form Controls Match:   [PASS / FAIL / N/A]
Check 6 — No console.log:        [PASS / FAIL]
Check 7 — No Blocking TODOs:     [PASS / FAIL]

----------------------------------------------
Result: [PASS (Exit 0) / FAIL (Exit 1)]

[If FAIL:]
Issues to resolve before jest-generator:
  - Check [N]: [description of fix needed]
==============================================
```

---

## Exit Conditions

- **Exit 0 (all PASS or N/A)**: Report result to calling agent; calling agent proceeds to Module 11b.
- **Exit 1 (any FAIL)**: Report result to calling agent; calling agent STOPS, presents failures to user, fixes issues, then re-invokes this skill.

---

## Implementation Note for Agent

This skill does NOT run a shell script (no terminal required). Instead, the agent performs each check by reading files and searching content using available file tools. Present the verification report in the chat output exactly as formatted above.
