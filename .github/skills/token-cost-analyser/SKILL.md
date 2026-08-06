---
name: token-cost-analyser
description: 'Captures LLM token usage per agent run, calculates USD cost using current model pricing for Anthropic, OpenAI, and Google, and writes a terse caveman-style cost report to .github/reports/. Zero PII — only token counts, model IDs, and timing captured.'
maturity: beta
maturity_since: "2026-08-06"
---

# Token Cost Analyser

Tracks LLM token usage per agent run. Calculates cost from `data/model-pricing.json`. Writes a terse report to `.github/reports/`.

## End-of-Run Usage (Every Agent)

At the end of every run call:

```bash
node .github/skills/token-cost-analyser/scripts/generate-cost-report.mjs \
  --agent-id <agent-name> \
  --model <model-id> \
  --input-tokens <N> \
  --output-tokens <N> \
  --cache-read <N> \
  --cache-write <N> \
  --report-dir .github/reports
```

Report written to `.github/reports/cost-report-<timestamp>-<agent>.txt`.

## Ingest from Log Files

### VS Code Copilot debug log
```bash
node .github/skills/token-cost-analyser/scripts/ingest-copilot-log.mjs \
  --log ~/.vscode/logs/copilot-debug.log --session-id <uuid>

node .github/skills/token-cost-analyser/scripts/generate-cost-report.mjs \
  --events-file ~/.lending-telemetry/token-events.ndjson \
  --report-dir .github/reports
```

### GitHub Actions runner log
```bash
node .github/skills/token-cost-analyser/scripts/ingest-actions-log.mjs \
  --log $RUNNER_TEMP/lending-agent.log

node .github/skills/token-cost-analyser/scripts/generate-cost-report.mjs \
  --events-file ~/.lending-telemetry/token-events.ndjson \
  --report-dir .github/reports
```

## Report Format

```
=== TOKEN COST REPORT ===
DATE:   2026-08-06
EVENTS: 1

--- host-orchestrator ---
MODEL:  claude-sonnet-4-5
IN:     4,200 tok = $0.0000126
OUT:      880 tok = $0.0000132
CACHE:  11,000 tok = $0.0000033 (saved $0.0000297)
COST:   $0.0000291
TIME:   8200ms

=========================
TOTAL:  $0.0000291
SAVED:  $0.0000297
SRC:    2025-08-06
=========================
```

## Pricing Data

Lives in `data/model-pricing.json`. Covers Anthropic, OpenAI, and Google. Update this file when prices change.

| Provider | Coverage | Last Updated |
|----------|----------|--------------|
| Anthropic | Claude Fable 5, Opus 5, Sonnet 5, Haiku 4.5, and legacy Sonnet/Opus | 2025-08-06 |
| OpenAI | GPT-4o, o3, o4-mini, o1, GPT-4 families | 2025-08-06 |
| Google | Gemini 2.5 Pro/Flash, 2.0 Flash, 1.5 families | 2025-08-06 |

## Configuration

| Env var | Default | Purpose |
|---------|---------|---------|
| `LENDING_TELEMETRY_SINK` | `~/.lending-telemetry/token-events.ndjson` | NDJSON event log sink |

## Guardrails

- No prompt content — schema prohibits `prompt`, `content`, `code`, `pii` fields
- Cost figures are estimates — actual billing may vary by plan and region
- Nothing leaves local machine unless `LENDING_TELEMETRY_SINK` is set to a remote URI
