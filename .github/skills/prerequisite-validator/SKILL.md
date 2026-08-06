# Prerequisite Validator Skill

## Purpose
Validate that all required inputs are present before invoking a specialist agent. Interactively collect missing information from the user with clear, helpful prompts.

## When to Use
- After intent classification, before agent invocation
- User request is missing required context
- Need to validate input format (URLs, ticket keys, etc.)

## Required Inputs by Domain

| Domain | Required Inputs | Format | Validation |
|---|---|---|---|
| `UI_IMPLEMENTATION` | Figma URL | `https://www.figma.com/file/...` or `https://www.figma.com/design/...` | Must be complete URL, not placeholder |
| `UI_IMPLEMENTATION` | Feature name | String (e.g., "loan application") | Non-empty |
| `REQUIREMENTS` | Jira ticket key | `[A-Z]+-\d+` (e.g., `PROJ-123`) | Matches pattern |

## Interaction Rules

### ❌ Bad Prompts
- "Missing: Figma URL"
- "Provide inputs"
- "Need more info"

### ✅ Good Prompts
```
To build this Angular screen, I need two pieces of information:

1. **Figma design URL** — I'll extract exact button labels, colors, spacing, and component structure from the design file.
   Format: `https://www.figma.com/file/abc123/Design-Name?node-id=...`

2. **Feature name** — Used for file and component naming.
   Example: "loan application", "customer dashboard"

Could you provide these?
```

## Validation Workflow

1. **Parse detected inputs** from intent classifier
2. **Check each required input**:
   - Present? → Validate format
   - Missing? → Add to missing list
3. **If missing inputs exist**:
   - Present clear prompt with examples
   - Explain **what** is needed and **why**
   - Wait for user response
4. **Validate user response**:
   - Correct format? → Proceed
   - Invalid format? → Re-prompt with specific error
5. **All valid?** → Return validated inputs

## Example: Figma URL Validation

**User input:** `https://figma.com/...`

**Validation result:** ❌ Invalid (placeholder)

**Prompt:**
```
I see you provided a Figma URL, but it appears to be a placeholder ("https://figma.com/...").

I need the complete URL to access the design. Here's an example of a valid URL:
`https://www.figma.com/file/abc123def456/Loan-App-Design?node-id=1%3A2`

You can copy this from your browser's address bar when viewing the Figma file.

Please share the complete URL.
```

## Output Format

Return validated inputs ready for agent invocation:

```json
{
  "status": "valid",
  "inputs": {
    "figma_url": "https://www.figma.com/file/abc123/...",
    "feature_name": "loan application screen"
  }
}
```

Or if missing:

```json
{
  "status": "missing",
  "missing": ["figma_url"],
  "prompt": "To build this Angular screen, I need..."
}
```

## Format Validators

### Figma URL
```regex
^https:\/\/(www\.)?figma\.com\/(file|design)\/[a-zA-Z0-9]+\/
```

### Jira Ticket Key
```regex
^[A-Z][A-Z0-9]+-\d+$
```

## Error Handling

- **Incomplete URL** → Explain what's missing, provide example
- **Wrong URL format** → Show correct format with example
- **Empty value** → Explain why it's needed
- **User says "I don't have it"** → Offer alternatives or explain that the agent cannot proceed without it
