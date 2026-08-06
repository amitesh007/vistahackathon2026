# Module 1: Figma Extraction & Design Specifications

## Purpose
Extract design specifications from ALL provided Figma URLs using design context (NOT screenshots).

## CRITICAL PRINCIPLE
**Design context is the PRIMARY source for ALL element details.**
- Fetch design context: `mcp__figma__get_design_context`
- Extract ALL details from JSX/component structure
- Do NOT assume element details from screenshots
- Screenshots are for visual confirmation ONLY

## Inputs
- `{FIGMA_URL_LIST}`: Array of Figma URLs provided by user
- Each URL may correspond to a specific screen section (form, table, success state, etc.)

## CRITICAL: Do Not Assume or Hallucinate

**Before starting:**
- If Figma URLs are unclear or incomplete → STOP and ASK user
- If node-id format is ambiguous → STOP and ASK user
- If unsure which sections exist → STOP and ASK user
- Go ONLY by what user provides — never infer missing URLs

**Example questions to ask:**
- "Could you provide all Figma URLs for this screen (including any success/confirmation states)?"
- "I received [X] URLs. Can you confirm which section each URL represents?"

---

## Process

### Step 1.1: Figma URL Collection
1. Receive all Figma URLs from user.
2. If URLs are incomplete or unclear → STOP and ASK user.
3. Identify which URL corresponds to which section.
4. Document URL mapping:
   ```
   Section → Figma URL → Node ID
   [Section name] → https://figma.com/.../node-id=XXXXX
   [Section name] → https://figma.com/.../node-id=YYYYY
   ```

### Step 1.2: Figma Design Context Extraction (PRIMARY SOURCE)

**Design context contains ALL element details — use this, NOT screenshots.**

For EACH URL in `{FIGMA_URL_LIST}`:
1. Call `mcp__figma__get_design_context` with the specific node ID.
2. Save the complete JSX/component structure output.
3. Extract element-level details:

   **Screen Identity:**
   - Screen/page title (from top-level heading: `<h1>`, `<h2>`, or main heading element)
   - Subtitle or description text
   - Breadcrumb path (if shown)
   - This title becomes the Angular component's `title` or `pageTitle` property.

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
   - Apparent action: submit form / navigate / open dialog / cancel

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

### Step 1.3: Screenshot for Visual Reference

Call `mcp__figma__get_screenshot` for each URL. Store for visual confirmation ONLY.
Do NOT extract text content from screenshots.

### Step 1.4: Validate Screen Type and Prompt for Missing URLs

**Identify the screen type from extracted content:**
- **Form screen**: primary purpose is data entry (has labeled inputs, a submit button)
- **List/Table screen**: primary purpose is displaying records (has column headers, rows, search/filter)
- **Detail/View screen**: primary purpose is showing read-only data for one record
- **Dashboard/Overview**: multiple summary tiles or cards
- **Dialog/Modal**: designed to appear as an overlay
- **Wizard/Stepper**: multi-step process with sequential steps

**If content suggests a screen type but its Figma URL is missing, STOP and prompt:**
```
I see content suggesting a [type] section, but no Figma URL was provided for it.
Please either:
A) Provide the Figma URL for the [type] section
B) Confirm this section is out of scope for this implementation
```

### Step 1.5: Identify Gaps

After extraction, list any elements that are:
- Visible in screenshots but NOT found in design context (flag for user clarification)
- Ambiguous in type (e.g., date field vs plain text with date format)
- Unclear in validation rules

If gaps exist → STOP and ask user before proceeding to Module 2.

---

## STOP Gate 1 Pre-Check

Before presenting the completion report, confirm ALL of the following:
- [ ] Design context fetched for EVERY provided Figma URL
- [ ] Screen title extracted (exact text)
- [ ] All sections and their purposes documented
- [ ] All form fields documented with label, type, required/optional
- [ ] All buttons have exact label text (not assumed)
- [ ] All icons identified
- [ ] Screen type determined
- [ ] All gaps listed (or "none")

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report before proceeding to Module 2.**

```
✅ MODULE 1 COMPLETION REPORT: Figma Extraction

1. Figma URLs Processed:
   - Section 1 ([name]): [URL] → [SUCCESS/FAILED]
   - Section 2 ([name]): [URL] → [SUCCESS/FAILED]
   (continue for all URLs)

2. Design Context Extraction:
   - Total sections extracted: [number]
   - Total form fields identified: [number] with labels: [list]
   - Total buttons identified: [number]
   - Errors encountered: [YES/NO — if YES, list]

3. Screen Type:
   - Determined type: [form / list-table / detail-view / dashboard / dialog / wizard]
   - Layout: [single-page / stepper / tabs / dialog]
   - Layout confirmed by: [design context / user]

4. Key Elements Documented:
   - Screen title: "[EXACT TEXT]"
   - Form fields with types: [YES/NO]
   - Button labels extracted: [YES/NO]
   - Icons identified: [YES/NO]

5. Gaps / Open Questions:
   - [list any ambiguous elements, or "None"]
   - Questions asked to user: [list or "None"]

✅ Module 1 Status: [COMPLETE/INCOMPLETE]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 2
```

**DO NOT proceed to Module 2 until user confirms this report.**

---

## Next Module
Proceed to **Module 2: Reference Pattern Analysis**.
