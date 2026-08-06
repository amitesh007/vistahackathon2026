# Intent Classifier Skill

## Purpose
Analyze user requests and classify them into workflow domains for the Host Orchestrator Agent.

## When to Use
- User makes a request to the Host Agent
- Need to determine which specialist agent(s) should handle the request
- Multi-domain requests need to be decomposed

## Workflow Domains

| Domain | Keywords / Patterns | Target Agent |
|---|---|---|
| `UI_IMPLEMENTATION` | "Angular screen", "Figma design", "UI component", "frontend feature", "build UI", "implement screen", "create component" | `ANGULAR_UI` |
| `REQUIREMENTS` | "Jira ticket", "requirements", "acceptance criteria", "story details", ticket key pattern `[A-Z]+-\d+` | `SDLC_PLANNER` |
| `MULTI_DOMAIN` | Requests spanning multiple domains (e.g., "build screen from Jira ticket ABC-123") | Multiple agents in sequence |
| `UNKNOWN` | No clear domain match | Ask user for clarification |

## Classification Rules

1. **Exact keyword match** — scan for domain-specific keywords
2. **Pattern detection** — look for Figma URLs (`figma.com`), Jira keys (`PROJ-123`)
3. **Dependency analysis** — if request mentions both requirements AND implementation, classify as `MULTI_DOMAIN`
4. **Ambiguity handling** — if unclear, classify as `UNKNOWN` and list available agents

## Output Format

Return a structured classification:

```json
{
  "domain": "UI_IMPLEMENTATION",
  "confidence": "high",
  "detected_inputs": {
    "figma_url": "https://figma.com/...",
    "feature_name": "loan application screen"
  },
  "missing_inputs": ["angular_version"],
  "target_agent": "ANGULAR_UI"
}
```

## Multi-Domain Example

**Input:** "Build the loan screen from Jira ticket LMS-42"

**Output:**
```json
{
  "domain": "MULTI_DOMAIN",
  "confidence": "high",
  "workflow": [
    {
      "step": 1,
      "domain": "REQUIREMENTS",
      "agent": "SDLC_PLANNER",
      "inputs": {"jira_key": "LMS-42"}
    },
    {
      "step": 2,
      "domain": "UI_IMPLEMENTATION",
      "agent": "ANGULAR_UI",
      "inputs": {"requirements_doc": "output from step 1"}
    }
  ]
}
```

## Edge Cases

- **Vague requests** ("help me build something") → `UNKNOWN`, ask for details
- **Non-software requests** ("what time is it?") → `UNKNOWN`, explain available capabilities
- **Requests for unavailable agents** → `UNKNOWN`, list available agents
