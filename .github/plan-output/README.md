# plan-output — SDLC Planner outputs & Orchestrator handoff contract

This folder holds everything the **`SDLC_PLANNER`** agent produces, and the contract that connects the two halves of the SDLC harness:

```
Jira story ──▶ SDLC_PLANNER ──writes──▶ .github/plan-output/<KEY>/ ──reads──▶ Orchestrator ──dispatches──▶ UI / API / QA / Deploy sub-agents
                (planner.agent.md)         (this folder)                        (follow-on)
```

## Per-story folder layout

Each Jira key gets one self-contained subfolder:

```
.github/plan-output/<KEY>/
  <KEY>-requirements.md   # structured requirements
  <KEY>-spec.md           # user stories, FS-XX, API contracts, data model, NFRs
  <KEY>-testcases.md      # unit / integration / e2e / negative tests + automation notes
  <KEY>-design.md         # Mermaid C4 / sequence / dataflow / deployment diagrams
  PLAN-<KEY>.md           # machine YAML contract + human tables (the Orchestrator's input)
```

- **`_PLAN_TEMPLATE.md`** (this folder) — the canonical template every `PLAN-<KEY>.md` is generated from.
- The four analysis artifacts are the **source of truth** for the tasks in `PLAN-<KEY>.md` — task `inputs` cite them by name.

The Planner is built today (`.github/agents/planner.agent.md`). The **Orchestrator is specified here but not yet implemented** — this document is its contract so the handoff is unambiguous when it is built.

---

## Plan lifecycle (`status`)

| status | Set by | Meaning |
|--------|--------|---------|
| `draft` | Planner | Plan written, awaiting human review. **Orchestrator must refuse to run.** |
| `approved` | **Human** (only) | Reviewed and signed off. The Orchestrator may begin. |
| `in-progress` | Orchestrator | Execution started; tasks transitioning. |
| `done` | Orchestrator | All tasks closed and gates green. |

The Planner never sets `approved` — that human checkpoint is the trust boundary of the whole system.

---

## Field dictionary (YAML frontmatter)

### Top level
| Field | Type | Notes |
|-------|------|-------|
| `plan_id` | string | `PLAN-<jira_key>`, unique. |
| `jira_key` / `jira_url` | string | Link back to the source story (audit traceability). |
| `title` | string | From the Jira summary. |
| `issue_type` | enum | `Story` \| `Bug` \| `Task` \| `Epic`. |
| `story_points` | int \| null | From Jira; `null` if unset. |
| `created` | date | `YYYY-MM-DD`. |
| `status` | enum | See lifecycle above. |
| `tracks` | list | Subset of `[ui, api, qa, deploy]` — only tracks with a concrete repo target. |
| `artifacts` | map | The four sibling analysis files (`requirements`, `spec`, `testcases`, `design`). |
| `tasks` | list | The execution DAG — see below. |
| `human_checkpoints` | list | `{ after, reason }` — where the run pauses for a human. |
| `governance` | map | `{ enforcement[], escalation, audit }` — the enforcement layer. |

### `tasks[]`
| Field | Type | Notes |
|-------|------|-------|
| `id` | string | `<track>-<n>`, unique within the plan. |
| `track` | enum | `ui` \| `api` \| `qa` \| `deploy`. |
| `agent` | enum | `ANGULAR_UI` (exists) \| `API` \| `QA` \| `DEPLOY`. Resolved to a real agent by the Orchestrator. |
| `title` | string | Human-readable task description. |
| `inputs` | map | Free-form: `spec`, `tests`, `design`, `contract`, `entity`, `figma_url`, `feature_path`, `angular_version`, `ui_library`, … |
| `targets` | list | Real repo paths the task touches. |
| `depends_on` | list | Task ids that must finish first. `[]` = ready immediately. **Defines execution order.** |
| `acceptance` | list | `AC-n` ids this task satisfies. |
| `gate` | list | Checks to close the task: `build` \| `test` \| `lint`. |
| `checkpoint` | enum | `none` \| `human`. `human` → Orchestrator stops for sign-off after this task. |
| `est_effort` | enum | `S` \| `M` \| `L` — directional cost signal. |

---

## Orchestrator contract (spec — how the follow-on agent consumes a plan)

An Orchestrator agent that reads a `PLAN-<KEY>.md` **must**:

1. **Refuse unless `status: approved`.** A `draft` plan is not runnable.
2. **Resolve `agent`** to a real agent: `ANGULAR_UI` → `.github/agents/ui-orchestrator.agent.md` (built); `API` / `QA` / `DEPLOY` → follow-on agents (not built yet).
3. **Execute in dependency order.** Run any task whose `depends_on` are all `done`; respect the DAG (never run a task before its dependencies).
4. **Dispatch one sub-agent per task** via `runSubagent`, passing the task's `inputs` (including the cited analysis artifacts) + `targets`.
5. **Enforce gates.** A task closes only when its `gate` checks pass and its `acceptance` (`AC-n`) hold. Apply `governance.enforcement` (e.g. no deploy until QA is green).
6. **Pause at human checkpoints.** Stop at every task with `checkpoint: human` and at each `human_checkpoints` entry; wait for sign-off before continuing.
7. **Escalate** per `governance.escalation` on repeated gate failure.
8. **Write the audit trail.** Append one timestamped line per task transition to the plan's `## Audit / Run Log`:
   `[<ISO-8601>] <task-id> <status> — <agent> — <gate result / note>`
9. **Advance `status`** `approved → in-progress → done`.

This mapping — deterministic YAML contract, gate enforcement, human checkpoints, and an append-only run log — is what makes the harness **reliable, governed, and auditable** rather than a one-shot demo.

---

## How the Planner is invoked

Point GitHub Copilot at the `SDLC_PLANNER` agent and give it a Jira key (e.g. `LOAN-123`). It reads the story via the Jira MCP (paste fallback if the MCP isn't up yet), writes the four analysis artifacts + `PLAN-LOAN-123.md` into `.github/plan-output/LOAN-123/` with `status: draft`, and stops at the approval checkpoint. See `.github/agents/planner.agent.md`.
