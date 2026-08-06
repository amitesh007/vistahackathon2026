# Angular UI Pattern Registry

This registry indexes reusable screen patterns for the `angular-ui` agent.
Scan this file during Module 0.5 — do NOT load pattern files until a match is confirmed.

## How to Use

1. Filter rows by the feature type / screen type from Module 0 context.
2. If a row's Tags overlap with the confirmed screen type, it is a candidate match.
3. Present matches to user before loading any pattern file.

---

## Directory Structure

```
.github/agents/modules/patterns/
├── form-screens/         # Create/Edit forms
├── table-screens/        # Data tables with search/filter
├── dialog-screens/       # Modal/dialog screens
├── wizard-screens/       # Multi-step forms
├── dashboard-tiles/      # Dashboard card/tile components
└── common_patterns/      # Cross-feature utilities
```

---

## Implemented Patterns

| ID | Screen | Tags | File |
|----|--------|------|------|
| UI-001 | _Example: Create Entity Form_ | `form`, `create`, `reactive-forms`, `http-post` | `patterns/form-screens/UI-001-create-entity-form.md` |
| UI-002 | _Example: Data Table with Search_ | `table`, `search`, `pagination`, `http-get` | `patterns/table-screens/UI-002-data-table-search.md` |

> The registry starts with examples. Add real patterns after implementing screens using Module 11.5.

---

## Pattern File Template

When registering a new pattern (Module 11.5), use `_PATTERN_TEMPLATE.md` in this folder.

---

## Tag Taxonomy

Use these tags when registering new patterns:

**Screen type:** `form`, `table`, `dialog`, `wizard`, `dashboard`, `detail-view`, `list-view`
**Operation:** `create`, `read`, `update`, `delete`, `search`, `export`
**Form type:** `reactive-forms`, `template-driven`
**API:** `http-get`, `http-post`, `http-put`, `http-delete`, `no-api`
**State:** `signals`, `rxjs`, `ngrx`, `no-state`
**UI library:** `angular-material`, `primeng`, `plain-scss`
