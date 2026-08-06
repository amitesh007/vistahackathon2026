# Module 3: Button Label Extraction from Figma

## CRITICAL MODULE — MANDATORY PROCESS

## Purpose
Extract EXACT button labels from Figma design context (not screenshots, not assumptions, not reference files).

## CRITICAL SOURCE: Design Context, NOT Screenshots
- Search design context for `<button>` components and `<p>` / text elements
- Extract exact text from JSX structure
- Do NOT read text from screenshots (can be unclear/pixelated)
- Do NOT assume labels from reference files
- Do NOT guess or infer

## Why This Module Exists
**Common mistake:** Assuming "standard" button labels or reading from screenshots.
- Example: Assuming "Cancel" when design context shows "Discard"
- Example: Copying "Submit" from reference when design context shows "Create Account"
- Example: Misreading screenshot when design context has exact text

---

## Process

### Step 3.1: Review Design Context Output (from Module 1)

Open the design context JSX structure saved in Module 1 for EACH section.

**If design context is missing button text → STOP and ASK user.**

Design context typically contains button text as:
```typescript
// Example from design context:
<button className="..." data-name="Button Primary">
  <p className="..." data-node-id="...">Create Account</p>   // ← EXACT label here
</button>
<button className="..." data-name="Button Secondary">
  <p className="..." data-node-id="...">Cancel</p>            // ← EXACT label here
</button>
```

### Step 3.2: Search Design Context for All Buttons

For EACH section's design context output:
1. Search for `<button` tags in the JSX structure
2. Search for `data-name="Button"` attributes
3. Search for `<p>` elements inside button components
4. Search for button container nodes with text children

**For EACH button found, extract with precision:**
- **Capitalization**: "Create Account" vs "create account"
- **Special characters**: "Save & Continue" (includes "&", not "and")
- **Spacing**: Exact spacing between words
- **Punctuation**: Any commas, ellipsis, exclamation marks

### Step 3.3: Identify Button Visual Variant

Map each button to its visual type by examining the design context class names or `data-name`:
- **Primary / Contained** (filled background): submit/save/confirm actions
- **Secondary / Outlined** (border only): navigate back / alternative actions
- **Text / Ghost** (no border, no fill): cancel / dismiss / minor actions
- **Icon-only**: action buttons with only an icon (toolbar, table row actions)
- **FAB** (Floating Action Button): primary screen-level create action

### Step 3.4: Document All Button Actions

For each button, also document its intended action:
- **Submit form** (triggers validation and API call)
- **Navigate** (goes to another route)
- **Open dialog** (launches a modal)
- **Cancel / Close** (discards current state and navigates back)
- **Toggle** (shows/hides something on screen)
- **Download / Export**

### Step 3.5: Distinguish Interactive vs Decorative

Note if any button is:
- **Disabled** by default: document the enable condition
- **Hidden** until a condition is met: document the condition
- **Icon-only** (needs `aria-label`): note the required accessible label

### Step 3.6: Document Extracted Labels

Create complete button documentation:

```
BUTTON INVENTORY — [Screen Name]

Section: [Section Name]
  Button 1:
    Label: "[EXACT TEXT from Figma]"
    Variant: [primary / secondary / text / icon-only]
    Position: [form footer right / toolbar / inline]
    Action: [submit / navigate / open dialog / cancel]
    Disabled condition: [none / form invalid / loading]
    Icon (if icon-only): [icon name]

  Button 2:
    Label: "[EXACT TEXT from Figma]"
    Variant: [...]
    ...
```

**If ANY button label is unclear in Figma context:**
- Do NOT assume standard labels
- Do NOT copy from reference files
- STOP and ASK user:
  > "The button label at [position] in [section] is not clear in the Figma context. Could you confirm the exact label text?"

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report before proceeding to Module 4.**

```
✅ MODULE 3 COMPLETION REPORT: Button Label Extraction

1. Sections Analyzed: [number]

2. Extracted Button Labels:
   Section 1 — [name]:
     - "[EXACT TEXT]" → [variant] → [action]
     - "[EXACT TEXT]" → [variant] → [action]
   Section 2 — [name]:
     - "[EXACT TEXT]" → [variant] → [action]
   (continue for all sections)

3. Special Characters / Formatting Verified:
   - Capitalization exact: [CONFIRMED]
   - Special characters ("&", "...", etc.): [DOCUMENTED or "none"]
   - Spacing exact: [CONFIRMED]

4. Source Verification:
   - Extracted from Figma design context: [CONFIRMED]
   - NOT copied from reference files: [CONFIRMED]
   - NOT assumed "standard" labels: [CONFIRMED]
   - NOT read from screenshots alone: [CONFIRMED]

5. Unclear Items Asked to User:
   - Questions asked: [number or NONE]
   - Items clarified: [list or N/A]

✅ Module 3 Status: [COMPLETE/INCOMPLETE]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 4
```

**DO NOT proceed to Module 4 until user confirms this report.**

---

## Next Module
Proceed to **Module 4: Action Icons Verification**.
