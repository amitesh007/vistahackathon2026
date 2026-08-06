# SDLC Harness — Workflows

This folder holds the CI/CD entry point for the SDLC multi-agent delivery harness.

The key idea: **CI/CD is the orchestrator.** Rather than an LLM deciding what runs
next, the GitHub Actions workflow sequences the specialized agents as jobs
(a deterministic DAG via `needs:`), enforces build/test gates as real jobs, and
commits an append-only audit trail. Deterministic control flow is what makes the
harness *reliable, governed, and auditable* instead of a one-shot demo.

| File | Purpose |
|------|---------|
| `run-orchestrator-agent.yaml` | The **SDLC Harness** pipeline — plan → execute → deploy, with two human checkpoints. |
| `build-push.yaml` | Container build → GHCR → Helm → AKS. Reused by the deploy phase. |
| `run-calculator-agent.yaml` | Reference pattern for invoking a Copilot custom agent from Actions. |

---

## The pipeline: 3 phases, 2 human checkpoints

The harness is dispatched manually (`workflow_dispatch`) with two inputs:
`jira_key` and `mode`. Each `mode` is one phase. Because we do **not** have repo
settings access (so GitHub Environments / required reviewers are unavailable), the
human checkpoints are implemented as **separate dispatch phases** — a human
dispatching the next phase *is* the approval. This keeps the gate real without any
settings.

```
mode=plan       job: plan
                Planner (host-orchestrator) writes PLAN-<KEY>.md (status: draft),
                commits it, and stops.

  ⛔ CHECKPOINT 1 — a human reviews the plan and sets `status: approved` in the
                    frontmatter, then commits.

mode=execute    jobs: guard-approved ─▶ ui ─┐
                                      ─▶ api ─┴─▶ qa
                guard-approved refuses to run unless `status: approved`.
                ui and api run in parallel; qa `needs:` both.
                On QA green, qa stamps `qa_gate: passed` into the plan and commits.

  ⛔ CHECKPOINT 2 — a human reviews the QA result and dispatches the deploy phase.

mode=deploy     jobs: guard-deploy ─▶ deploy
                guard-deploy refuses to run unless `status: approved`
                AND `qa_gate: passed` (the "no deploy until QA is green" rule).
```

---

## How to run it

From the Actions tab (**SDLC Harness → Run workflow**) or the CLI:

```bash
# Phase 1 — plan
gh workflow run "SDLC Harness" -f jira_key=MPSSC-43084 -f mode=plan
```

Review `.github/agents/plans/PLAN-MPSSC-43084.md`, set `status: approved`, commit.

```bash
# Phase 2 — execute (ui + api + qa gate)
gh workflow run "SDLC Harness" -f jira_key=MPSSC-43084 -f mode=execute
```

Confirm `qa_gate: passed` landed in the plan, then:

```bash
# Phase 3 — deploy
gh workflow run "SDLC Harness" -f jira_key=MPSSC-43084 -f mode=deploy
```

Each phase prints its next-step command in the run summary.

---

## How this maps to the hackathon scoring criteria

| Criterion | Where it lives in the harness |
|-----------|-------------------------------|
| **Harness** — a rig that runs the workflow | `run-orchestrator-agent.yaml` sequences the agents as a DAG. |
| **Reliability** — holds across repeated trials | Deterministic `needs:` DAG + real gate jobs — same result every run. |
| **Governance — enforcement layer** | `guard-approved` and `guard-deploy` jobs fail the run when preconditions aren't met. |
| **Governance — escalation to human checkpoint** | Two dispatch-phase checkpoints; the pipeline cannot advance without a human. |
| **Governance — audit trail** | Every phase commits to the plan (draft → `qa_gate` → deploy log) plus per-job Actions logs. |
| **Governance — no deploy until QA green** | `guard-deploy` requires `qa_gate: passed`. |
| **Cost** | Directional `est_effort` rollup carried in the plan document (see `plans/README.md`). |

---

## Current status — what's real vs stubbed

**Real and wired:**
- The 3-phase / 2-checkpoint structure and both approval guards.
- The `qa_gate: passed` marker and all audit commits.
- Agent calls for the Planner (`host-orchestrator`) and UI (`ui-orchestrator`).

**Stubbed (agent not built yet — clearly marked in the workflow):**
- `api`, `qa`, `deploy` jobs echo a `::warning::` instead of calling an agent.
  Swap the agent slug into the `curl` call when each agent lands.

**Gate commands (build/test):**
- The real `npm run build` (UI) and `./gradlew test` (API) gate steps are present
  but commented out, with their `setup-node` / `setup-java` steps ready to enable.

---

## Known limitations to resolve

1. **Copilot completions API returns text, not file edits.**
   `POST /copilot/agents/<slug>/completions` returns a chat message; it does **not**
   modify the runner's checked-out repo. For agents that must actually write code
   (UI/API), use the Copilot coding agent / CLI in the job instead of the completions
   endpoint. The `curl` blocks are the integration points where that swap happens.

2. **`host-orchestrator` must honor `mode`.**
   In plan mode the agent must produce `PLAN-<KEY>.md` and stop without dispatching;
   in execute mode it (or the CI jobs) run the tracks. Confirm the agent branches on
   the `plan` / `execute` wording in the prompt.

3. **Agent slugs assume the filename base.**
   The completions URL uses the agent file's base name (e.g. `host-orchestrator.agent.md`
   → `host-orchestrator`), matching `run-calculator-agent.yaml`. Confirm when each
   agent file is merged to the default branch — the agent must be on the default
   branch to be callable.

4. **Internal Jira is unreachable from cloud runners.**
   `jira.finastra.com` resolves only inside the corporate network, so the Planner
   cannot read the story directly from a GitHub-hosted runner. Pass the story text
   into the run (e.g. via the issue body / prompt) or run the Planner where network
   access exists. The plan document is the hand-off either way.
