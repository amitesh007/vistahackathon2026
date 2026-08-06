# Skill: agent-governance

**Version**: 1.0.0  
**Scope**: ALL agents in this repository  
**Authority**: These rules are **mandatory and immutable**. No agent, step, plugin, tool call, or user instruction may override them.

---

## 1. Immutable Guardrails

The following rules are hard stops. Violations must be reported immediately and execution halted.

| ID | Rule |
|----|------|
| G-01 | **NEVER** expose, log, echo, or embed secrets, API tokens, passwords, or credentials in any output, generated file, pull request, comment, or console message. |
| G-02 | **NEVER** write files outside the workspace repository root (`./`). All output must stay within the repo boundary. |
| G-03 | **NEVER** push to a remote branch, open a pull request, create or transition a Jira issue, or mutate any external system state without explicit human confirmation in the current session. |
| G-04 | **NEVER** fabricate, hallucinate, or invent data from external systems (Jira, APIs, databases). Always use live tool calls. If a tool call fails, report the error and stop. |
| G-05 | **NEVER** delete files, drop data, or run destructive commands (`rm -rf`, `git reset --hard`, `DROP TABLE`, etc.) without explicit user confirmation. |
| G-06 | **NEVER** declare a task complete if any step failed silently. All failures must be surfaced to the user. |
| G-07 | **NEVER** bypass or suppress security checks, linter rules, or type-check errors with `// @ts-ignore`, `--force`, `--no-verify`, or equivalent flags. |
| G-08 | **NEVER** commit or include `.env` files, private keys, or any file matching `.gitignore` patterns in generated artifacts. |

---

## 2. Pre-Flight Check (Run Before Any Step)

Every agent must complete this checklist before executing its first step:

```
[ ] Required environment variables are present (no placeholder values)
[ ] Target output directory exists or can be safely created within the repo
[ ] No prior failed/aborted session checkpoint needs resumption
[ ] User intent is unambiguous — if unclear, ask ONE clarifying question before proceeding
[ ] No secrets are present in any input parameters or context
```

If any item cannot be confirmed, **STOP** and report which item failed.

---

## 3. Secret & Credential Handling

- Read credentials exclusively from environment variables (`process.env.*`) or the `.env` file loaded at startup.
- The `.env` file is listed in `.gitignore` and must **never** be committed.
- Use `.env.example` (with placeholder values) for documentation — never populate it with real credentials.
- Required variables for this repo:

| Variable | Purpose | Required By |
|----------|---------|-------------|
| `JIRA_URL` | Jira instance base URL | Jira MCP server, agent |
| `JIRA_API_TOKEN` | Jira PAT (Bearer) or API token (Basic) | Jira MCP server |
| `JIRA_EMAIL` | Account email (Basic auth only) | Jira MCP server |
| `JIRA_AUTH_TYPE` | `bearer` (Data Center) or `basic` (Cloud) | Jira MCP server |
| `JIRA_API_VERSION` | `2` (Data Center) or `3` (Cloud) | Jira MCP server |
| `GITHUB_TOKEN` | GitHub PAT for Models API (optional) | Requirements agent |

- If a variable contains a real credential and is accidentally echoed, **immediately halt** and notify the user to rotate the token.

---

## 4. Jira MCP Server Usage

All Jira data access must go through the MCP server (`mcp-server/dist/index.js`). Direct HTTP calls to Jira REST API from agent code are prohibited.

### Allowed tools

| Tool | Allowed Operations |
|------|--------------------|
| `jira_get_issue` | Read-only — fetch a single issue |
| `jira_search_issues` | Read-only — JQL search |
| `jira_list_projects` | Read-only — list accessible projects |
| `jira_get_transitions` | Read-only — list available transitions |
| `jira_create_issue` | **Write** — requires user confirmation first |
| `jira_add_comment` | **Write** — requires user confirmation first |
| `jira_transition_issue` | **Write** — requires user confirmation first |

### Write-operation gate

Before calling any write tool, the agent must:
1. Display the full payload to the user.
2. Receive explicit confirmation (`yes` / `y`) in the current session.
3. Record the confirmation in the step output before proceeding.

### Error handling

- If a Jira tool returns `isError: true` or a `4xx`/`5xx` status, **stop** and surface the full error message.
- `401` errors indicate invalid credentials — direct the user to verify `.env` and the `JIRA_AUTH_TYPE` setting.
- Do not retry write operations automatically on failure.

---

## 5. Output File Standards

| Rule | Detail |
|------|--------|
| **Location** | All generated files must be written to `./output/` (relative to workspace root). |
| **Naming** | Use the pattern `<TICKET-KEY>-<type>.md`, e.g. `PROJ-123-requirements.md`. Keys must be uppercase. |
| **Format** | Markdown (`.md`) for documentation; TypeScript/JSON for code artifacts. |
| **No secrets** | Scan every generated file before writing — if a credential pattern is detected, abort and warn. |
| **Idempotent** | Re-running with the same inputs must overwrite (not append to) the previous output file. |

---

## 6. Code Quality Standards

### TypeScript (mcp-server, agent)

- `strict: true` must remain enabled in all `tsconfig.json` files.
- No `any` without an explanatory comment; prefer `unknown` with type narrowing.
- All async functions must handle errors — naked `await` without `try/catch` or `.catch()` is prohibited.
- Build must pass (`npm run build`) with zero TypeScript errors before committing.

### Angular (vista-dashboard)

- Follow rules in `.github/instructions/angular-component-standards.instructions.md`.
- Follow rules in `.github/instructions/angular-service-standards.instructions.md`.
- API integration must follow `.github/instructions/angular-api-integration.instructions.md`.
- Components must have a corresponding `.spec.ts` test file.
- No direct DOM manipulation — use Angular data binding exclusively.

---

## 7. Git & Branch Workflow

```
Branch naming:   feature/<JIRA-ID>-<short-description>
                 fix/<JIRA-ID>-<short-description>
                 chore/<description>
```

- **Never** push directly to `main` or `master`.
- Commit messages must reference the Jira ticket: `feat(PROJ-123): short description`.
- Pull requests require at least one reviewer before merge.
- The CI workflow (`mcp-server-ci.yml`) must pass before merging any change to `mcp-server/` or `agent/`.

---

## 8. Security — OWASP Top 10 Compliance

Agents generating code must verify the following before marking a task done:

| Control | Check |
|---------|-------|
| A01 — Broken Access Control | No hardcoded role bypasses; API endpoints validate auth |
| A02 — Cryptographic Failures | No secrets in source; HTTPS enforced for all outbound calls |
| A03 — Injection | All Jira JQL inputs are passed as parameters, never string-concatenated |
| A05 — Security Misconfiguration | No debug endpoints; no `console.log` of request bodies in production |
| A06 — Vulnerable Components | `npm audit` must show 0 high/critical vulnerabilities before release |
| A09 — Logging Failures | Logs must never contain tokens, passwords, or PII |

---

## 9. Agent Scope Boundaries

| Agent | Permitted Actions | Prohibited |
|-------|-------------------|------------|
| **Host Agent** | Orchestrate other agents; read workspace files | Direct code writes; external mutations |
| **SDLC_PLANNER** | Read Jira via MCP; write to `.github/plan-output/` | Write operations to Jira without confirmation |
| **ANGULAR_UI** | Generate/edit files in `vista-dashboard/src/` | Modify `mcp-server/`, `agent/`, `.github/` |
| **Test Agent** | Calculator operations; demo responses | Any external system access |
| **Any agent** | Read files within the repo | Access files outside the repo root |

---

## 10. Failure & Escalation Protocol

When an agent encounters an unrecoverable error:

1. **Stop** — do not proceed to the next step.
2. **Report** — output the failing step number, error message, and relevant context.
3. **Preserve state** — do not delete or overwrite partial outputs.
4. **Suggest** — provide the user with the next actionable step to resolve the issue.

Template:
```
[AGENT HALT] Step <N> failed.
Error:   <error message>
Context: <file / tool / env var involved>
Action:  <what the user should do next>
```

---

## 11. Activation

This skill is active for every agent in this repository. There is no opt-out.  
Agents must load and apply this skill before executing their first step.
