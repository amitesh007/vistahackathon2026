# Skill: figma-extractor

**Version**: 1.0.0
**Invoked by**: `angular-ui` agent — Module 1
**Purpose**: Extract complete design specifications from Figma URLs using MCP tools.

---

## When Invoked

The `angular-ui` agent invokes this skill at the start of Module 1 after the user provides Figma URLs.

---

## Workflow

### Step 1 — URL Validation

For each URL provided by the user:
- Confirm it is a Figma URL (`figma.com/file/...` or `figma.com/design/...`)
- Extract the `node-id` query parameter if present
- If multiple URLs provided, process each independently

### Step 2 — Design Context Extraction

For each URL, call `mcp__figma__get_design_context`:
```
Input: { url: "[figma URL]" }
```

From the returned context, extract and document:

**Screen Identity:**
- Screen / page title
- Subtitle or description text
- Breadcrumb path (if shown)

**Layout Structure:**
- Top-level layout: full-page / panel / dialog / stepper / tabs
- Main sections: identify named sections/panels and their purpose
- Column structure: single-column / two-column / sidebar + main

**Form Fields** (for each field):
- Label text (exact, from design context)
- Field type: text / number / email / date / select / multi-select / toggle / checkbox / radio / textarea / file-upload / autocomplete
- Required or optional (look for asterisk or "required" label)
- Placeholder text (if shown)
- Validation rules mentioned in design (max length, format, range)
- Helper/hint text below field (if shown)

**Buttons** (for each button):
- Exact label text (character-perfect)
- Visual variant: primary (filled) / secondary (outlined) / text / icon-only
- Position in layout (form footer / toolbar / inline / FAB)
- Apparent action: submit / cancel / navigate / open dialog / download

**Icons** (for each icon):
- Design system icon name (if identifiable) or description
- Context: standalone / inside button / inside input / decorative
- Interactive (clickable) or decorative

**Data Display Elements:**
- Tables: column headers (exact text), sortable/filterable indicators, row actions
- Lists: item structure, metadata shown per item
- Cards: title, body fields, footer actions
- Charts/graphs: type, data labels

**Navigation Elements:**
- Breadcrumbs
- Tabs (with exact tab labels)
- Stepper steps (with exact step labels and order)
- Back/Close buttons

**Dialogs/Modals** (if any):
- Trigger condition
- Title
- Body content
- Action buttons

### Step 3 — Screenshot for Visual Reference

For each URL, call `mcp__figma__get_screenshot`:
```
Input: { url: "[figma URL]" }
```
Store screenshot reference for visual comparison only. Do NOT extract text content from screenshots.

### Step 4 — Gap Identification

After extracting all contexts, identify:
- Elements visible in screenshots but NOT found in design context (flag for user clarification)
- Ambiguous field types (e.g., date vs text with date format)
- Unclear validation rules

### Step 5 — Design Spec Output

Produce a structured **Design Spec Document**:

```markdown
# Design Spec: [Screen Name]
Source: [Figma URL(s)]
Extracted: [timestamp]

## Layout
[Layout description]

## Sections
### [Section 1 Name]
[description]

### [Section 2 Name]
[description]

## Form Fields
| Label | Type | Required | Placeholder | Validation |
|-------|------|----------|-------------|------------|
| [label] | [type] | [Y/N] | [text] | [rules] |

## Buttons
| Label | Variant | Action |
|-------|---------|--------|
| [label] | [variant] | [action] |

## Icons
| Icon | Context | Interactive |
|------|---------|-------------|
| [name] | [context] | [Y/N] |

## Data Display
[description of tables, lists, cards]

## Navigation
[breadcrumbs, tabs, steppers]

## Open Questions
- [Any ambiguous elements needing user clarification]
```

---

## Output Contract

Returns the Design Spec Document to the calling agent (Module 1).
The calling agent presents it and lists open questions before Module 2.

---

## Error Handling

- If `mcp__figma__get_design_context` fails: note the failure, try `mcp__figma__get_metadata` as fallback, then ask user to share design specs manually
- If a URL is not accessible (permissions): STOP and ask user to share access or export specs
- Never proceed to Module 2 with unresolved critical gaps (missing form field labels, missing button labels)
