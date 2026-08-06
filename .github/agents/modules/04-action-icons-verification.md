# Module 4: Action Icons Verification

## CRITICAL MODULE — CONDITIONAL IMPLEMENTATION

## Purpose
Verify whether action icons (toolbar icons, row actions, floating icons) exist in the Figma design before implementing them. Prevent both omission AND false implementation.

## THE GOLDEN RULE
> **Check FIGMA design context, NEVER check reference files**
> **Reference files teach STRUCTURE. Figma defines CONTENT.**
> **Never check reference code to decide IF an element exists.**

## CRITICAL RULE
**Implement icons ONLY if they are present in the Figma design context.**
- Do NOT implement icons by default from reference patterns
- Do NOT copy icons from reference files without Figma verification
- Use `mcp__figma__get_design_context` to search YOUR Figma
- Search design context output for icon components
- Implement ONLY if icons found in Figma

---

## Process

### Step 4.1: Fetch Figma Design Context (PRIMARY SOURCE — MANDATORY)

For EACH section's Figma URL, call `mcp__figma__get_design_context`.

**Why Figma design context (NOT reference code):**
- Component names are explicit (e.g., `EditIcon`, `DeleteOutlined`, `MoreVertIcon`)
- Icon paths are documented (`edit.svg`, `delete.svg`, `more_vert.svg`)
- Shows what's actually in the design
- Reference code shows patterns, not YOUR screen's content

### Step 4.2: Search Design Context for Action Icons

Look for the following in design context output:

**Toolbar/Header Icons** (typically in screen top-right or within a header bar):
- Component names: `EditIcon`, `MoreVertIcon`, `SettingsIcon`, `FilterIcon`, `DownloadIcon`
- SVG references: `edit.svg`, `settings.svg`, `filter_list.svg`, `download.svg`
- Material Icon names: `"edit"`, `"settings"`, `"filter_list"`, `"more_vert"`

**Table Row Action Icons** (in a rightmost column of a table):
- Patterns: inline `<IconButton>`, `action-cell`, `row-actions`
- Common icons: Edit pencil, Delete trash, View eye, Expand chevron

**Form Field Icons** (suffix/prefix inside an input):
- Patterns: `matSuffix`, `matPrefix`, clear button (×), calendar icon, search icon

**Floating Action Button (FAB)**:
- Patterns: `mat-fab`, `p-speedDial`, `position: fixed` with `+` or `add` icon

**Decorative Icons** (purely visual, non-interactive):
- Patterns: success checkmark, error warning triangle, info circle beside a label

### Step 4.3: Determine Icon Purpose

For EACH icon found, document:
- **Icon name or component name** (from design context, not from assumption)
- **Interactive or decorative?**
  - Interactive: requires `click` handler, `aria-label`, keyboard access
  - Decorative: requires `aria-hidden="true"`, no click handler
- **Action triggered** (for interactive icons): edit record / delete record / open menu / filter / export / etc.
- **Position**: toolbar (top-right) / table cell / form field / floating button / sidebar

### Step 4.4: When to STOP and ASK User

**Ask if ANY of the following:**
- Icons are visible in screenshot but NOT found in design context (screenshot-only is ambiguous)
- Icon type is unclear (edit vs refresh vs sync — component name is the authority)
- Icons appear in reference files but YOUR Figma context does not include them
- You are uncertain whether an icon is interactive or decorative

**Example question to ask:**
```
ACTION REQUIRED — Please Clarify:

I searched the Figma design context for [section] and found [icon name / "no icon components"].

I can see [description] in the screenshot, but I cannot confirm whether this is:
A) An interactive action icon that needs a click handler
B) A decorative icon for visual emphasis only
C) Not present in this screen at all

Please confirm before I proceed to Module 5.
```

**NEVER DO:**
- "Reference patterns show a toolbar with icons, so I'll include them" ← WRONG
- "Most screens have an edit button, so this one probably does too" ← WRONG
- "The screenshot looks like there might be an icon in the corner" ← WRONG

### Step 4.5: Document Findings and Implementation Decision

For EACH section, document:

```
Icon Analysis — [Section Name]

Toolbar icons: [FOUND / NOT FOUND]
  - If found: [icon name] → [action] → [interactive/decorative]
  - If not found: SKIP — no toolbar icon group

Table row actions: [FOUND / NOT FOUND / N/A — not a table]
  - If found: [icon name] → [action]

Form field icons: [FOUND / NOT FOUND]
  - If found: [icon name] → [position: suffix/prefix] → [purpose]

FAB: [FOUND / NOT FOUND]
  - If found: [icon name] → [action]

Decorative icons: [number found]
  - [icon name] → decorative → aria-hidden="true"

Implementation decision: [IMPLEMENT listed icons / SKIP — no icons in Figma]
```

### Step 4.6: If Icons Are NOT Found

**Do NOT implement icon groups by default.**

If Figma context shows NO icons:
1. Document "No action icons found in Figma design context"
2. STOP and confirm with user:
   ```
   I did not find action icons in the Figma design context for [section/screen].
   
   Should I:
   A) Skip icons entirely (no icon elements generated)
   B) Include these specific icons: [list] — you confirm they belong to the design
   
   Please confirm before I proceed.
   ```

---

## MANDATORY COMPLETION REPORT

**YOU MUST provide this completion report before proceeding to Module 5.**

```
✅ MODULE 4 COMPLETION REPORT: Action Icons Verification

1. Design Context Search Results:
   - Icon components found: [list or "NONE"]
   - SVG/icon path references found: [list or "NONE"]
   - Screenshot cross-referenced: [YES/NO]

2. Section-by-Section Analysis:
   [Section name]:
     - Toolbar icons: [FOUND: list / NOT FOUND]
     - Table row icons: [FOUND: list / NOT FOUND / N/A]
     - Form field icons: [FOUND: list / NOT FOUND / N/A]
     - FAB: [FOUND / NOT FOUND]
     - Decorative icons: [N found]
     - Decision: [IMPLEMENT / SKIP]

3. Implementation Decisions:
   - Total interactive icons to implement: [number]
   - Total decorative icons (aria-hidden): [number]
   - Sections with no icons (skipped): [list or "none"]

4. User Clarification:
   - Unclear items: [list or NONE]
   - User confirmations received: [YES/NO/PENDING]

5. Anti-Pattern Verification:
   - Did NOT implement by default: [CONFIRMED]
   - Did NOT copy from reference without verification: [CONFIRMED]
   - Did NOT assume based on patterns: [CONFIRMED]
   - Implemented ONLY if found in Figma: [CONFIRMED]

✅ Module 4 Status: [COMPLETE/INCOMPLETE]

🛑 WAITING FOR USER CONFIRMATION TO PROCEED TO MODULE 5
```

**DO NOT proceed to Module 5 until user confirms this report.**

---

## Next Module
Proceed to **Module 5: Component Specification Generation**.
