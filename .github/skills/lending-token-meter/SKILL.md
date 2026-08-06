---
name: lending-token-meter
description: 'Captures raw token and model usage from every Lending BU agent invocation into a local NDJSON event log. Supports VS Code Copilot debug log and GitHub Actions runner log ingestion. Opt-in remote sink via LENDING_TELEMETRY_SINK env var. Never captures prompt text, source code, or PII.'
maturity: beta
maturity_since: "2026-05-30"
---

# Lending Token Meter Skill

Capture token usage events from Lending BU agent runs so cost can be attributed per agent, skill, team, and repo.

## When to Use

- **After every agent session** — ingest the Copilot debug log to record token usage.
- **In GitHub Actions** — ingest the runner log to capture CI agent runs.
- **Via the stitcher** — injected automatically when `cross_cutting.telemetry: token-meter+budget-guard` is set.

## How to Run

### Ingest a VS Code Copilot debug log:
```
node .github/skills/lending-token-meter/scripts/ingest-copilot-log.mjs \
  --log ~/.vscode/logs/copilot-debug.log \
  --session-id <uuid>
```

### Ingest a GitHub Actions runner log:
```
node .github/skills/lending-token-meter/scripts/ingest-actions-log.mjs \
  --log $RUNNER_TEMP/lending-agent.log
```

### Dry-run (inspect without writing):
```
node .github/skills/lending-token-meter/scripts/ingest-copilot-log.mjs \
  --log ~/.vscode/logs/copilot-debug.log --dry-run
```

## Output Format

```json
{ "ingested": 12, "skipped": 3, "sink": "~/.lending-telemetry/token-events.ndjson" }
```

### Event record shape (one per NDJSON line in the sink):
```json
{
  "ts": "2026-05-30T10:00:00Z",
  "session_id": "uuid",
  "agent_id": "lending-solution-architect",
  "step_id": 3,
  "skill_id": "lending-kb-extract-java",
  "model": "claude-sonnet-4.6",
  "tokens": { "input": 4200, "output": 880, "cache_read": 11000, "cache_write": 0 },
  "wall_clock_ms": 8200,
  "repo": "LendingBU/payments-service",
  "actor": "krijan.kothapalli",
  "trigger": "vscode-chat"
}
```

## Configuration

| Env var | Default | Purpose |
|---------|---------|---------|
| `LENDING_TELEMETRY_SINK` | `~/.lending-telemetry/token-events.ndjson` | Destination for events. Set to an `s3://` URL to push to remote storage. |

### Report Output

The report is written to the path returned as `report_path` in the tool's JSON output. It contains:
- Token totals by model
- Token totals by agent/skill
- Estimated USD cost per model
- Wall-clock time per step


## Guardrails

| Rule | Detail |
|------|--------|
| No prompt content | Event schema explicitly prohibits `prompt`, `content`, `code`, `pii` fields |
| Opt-in remote sink | Nothing leaves local machine unless `LENDING_TELEMETRY_SINK` is set |
| Offline-first | Works without network access; local NDJSON is always the primary sink |

## Troubleshooting

| Issue | Resolution |
|-------|-----------|
| `ingested: 0` | Log file may not contain Copilot token records — verify logLevel=debug is enabled |
| Permission error on sink | Check `~/.lending-telemetry/` permissions; script creates the dir but not the parent |
