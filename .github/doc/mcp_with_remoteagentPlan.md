# MCP Integration Plan: Figma MCP + Jira MCP with GitHub Remote Agents

**Repository:** `fin-lending/vistahackathon26`
**Goal:** Enable GitHub Copilot remote agents to connect to Figma MCP (official) and Jira MCP (local installable) so they can read designs and requirements, generate UI components, and raise PRs automatically.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Repository Structure to Create](#2-repository-structure-to-create)
3. [Step 1 — Commit Jira MCP Installable](#step-1--commit-jira-mcp-installable)
4. [Step 2 — Create mcp.json](#step-2--create-mcpjson)
5. [Step 3 — Configure GitHub Secrets](#step-3--configure-github-secrets)
6. [Step 4 — Create Agent Definition](#step-4--create-agent-definition)
7. [Step 5 — Create Workflow](#step-5--create-workflow)
8. [End-to-End Execution Flow](#end-to-end-execution-flow)
9. [Implementation Checklist](#implementation-checklist)

---

## 1. Overview

The framework wires three systems together:

| System | Role |
|---|---|
| **Figma MCP** (official, HTTP/SSE) | Provides design specs — frames, component properties, colours, layout |
| **Jira MCP** (local installable in `.github/JiraMCP`) | Provides requirements — acceptance criteria, story details, ticket metadata |
| **GitHub Copilot Remote Agent** | Reads both MCPs, generates UI code, and raises a PR |

Authentication uses **org-level GitHub secrets** — a single shared service account token for both Figma and Jira. No per-user PAT management is needed.

---

## 2. Repository Structure to Create

```
.github/
  copilot/
    mcp.json                      ← MCP server declarations consumed by remote agents
  JiraMCP/
    index.js  (or dist/)          ← Jira MCP installable (already exists locally)
    package.json                  ← Required so the workflow can run `npm install`
    .env.example                  ← Documents required env vars (no real values)
  agents/
    my-test-agent.agent.md        ← existing
    ui-agent.agent.md             ← NEW: UI generation agent definition
  workflows/
    build-push.yaml               ← existing
    run-calculator-agent.yaml     ← existing
    run-ui-agent.yaml             ← NEW: triggers the UI agent with Figma + Jira context
docs/
  mcp-integration-plan.md        ← this document
```

---

## Step 1 — Commit Jira MCP Installable

Copy your local Jira MCP files into `.github/JiraMCP/`:

```
.github/JiraMCP/
  package.json       ← must define "main" entry point and dependencies
  index.js           ← MCP server entry point (stdio transport)
  .env.example       ← template (see below — no real secrets committed)
```

**`.env.example` contents** (adjust keys to match your `.jira-mcp.env`):

```
JIRA_BASE_URL=https://yourorg.atlassian.net
JIRA_API_TOKEN=your-atlassian-api-token-here
JIRA_USER_EMAIL=service-account@yourorg.com
```

> ⚠️ Never commit the real `.jira-mcp.env` file. Add it to `.gitignore`.

---

## Step 2 — Create `mcp.json`

Create `.github/copilot/mcp.json`. This file is read by the GitHub Copilot agent runtime to know which MCP servers to connect to and how.

```json
{
  "mcpServers": {
    "figma": {
      "type": "http",
      "url": "https://mcp.figma.com/sse",
      "headers": {
        "X-Figma-Token": "${FIGMA_API_TOKEN}"
      }
    },
    "jira": {
      "type": "stdio",
      "command": "node",
      "args": [".github/JiraMCP/index.js"],
      "env": {
        "JIRA_BASE_URL": "${JIRA_BASE_URL}",
        "JIRA_API_TOKEN": "${JIRA_API_TOKEN}",
        "JIRA_USER_EMAIL": "${JIRA_USER_EMAIL}"
      }
    }
  }
}
```

**Key points:**

- `figma` uses `type: http` — the official Figma MCP is a remote server reachable via HTTP SSE. The `X-Figma-Token` header carries the Figma access token.
- `jira` uses `type: stdio` — your local installable is a Node.js process launched by the agent runtime. The agent starts the process and communicates over stdin/stdout.
- `${VAR_NAME}` placeholders are resolved at runtime from environment variables injected by the workflow.

---

## Step 3 — Configure GitHub Secrets

### 3a. Add Org-Level Secrets

Navigate to: **GitHub Org → Settings → Secrets and variables → Actions → New organization secret**

| Secret Name | Description |
|---|---|
| `FIGMA_API_TOKEN` | Figma org/service account access token (read scope: Files, Dev Resources) |
| `JIRA_BASE_URL` | e.g. `https://yourorg.atlassian.net` |
| `JIRA_API_TOKEN` | Atlassian API token for the service account |
| `JIRA_USER_EMAIL` | Email of the Atlassian service account |
| `COPILOT_TOKEN` | GitHub PAT or app token with `copilot` scope (already used in existing workflows) |

For each secret, set **Repository access** to include `fin-lending/vistahackathon26`.

### 3b. Enable Secrets for Copilot Agents

Navigate to: **GitHub Org → Settings → Copilot → Coding agent**

Grant the Copilot agent access to:
- `FIGMA_API_TOKEN`
- `JIRA_BASE_URL`
- `JIRA_API_TOKEN`
- `JIRA_USER_EMAIL`

> Without this step, the agent runtime cannot resolve `${VAR_NAME}` in `mcp.json`.

### 3c. Get a Figma Access Token

1. Log in to [figma.com](https://www.figma.com) with the service account
2. Go to **Settings → Security → Personal access tokens**
3. Create a token with scopes: **File content (read)**, **Dev resources (read)**
4. Store the token value as the `FIGMA_API_TOKEN` secret

---

## Step 4 — Create Agent Definition

Create `.github/agents/ui-agent.agent.md`:

```markdown
---
name: ui-agent
description: Generates Angular UI components from Figma designs and Jira requirements, then raises a PR
model: claude-sonnet-5
tools:
  - figma
  - jira
  - github
---

You are a UI generation agent for the Vista Dashboard (Angular application).

When invoked, you will receive:
- A Figma file URL or node ID containing the design
- A Jira ticket ID with the feature requirements

Your workflow:
1. Use the Figma MCP tool to fetch design specifications (layout, components, colours, typography) from the provided Figma URL
2. Use the Jira MCP tool to read the acceptance criteria and user story from the provided Jira ticket
3. Generate the corresponding Angular component(s) that satisfy both the design and the requirements
4. Place the generated files in the correct location within `vista-dashboard/src/`
5. Create a feature branch and raise a PR with:
   - A clear title referencing the Jira ticket
   - A description summarising what was generated and why
   - Links to the source Figma frame and Jira ticket
```

---

## Step 5 — Create Workflow

Create `.github/workflows/run-ui-agent.yaml`:

```yaml
name: Run UI Agent (Figma + Jira)

on:
  workflow_dispatch:
    inputs:
      figma_url:
        description: "Figma file URL or node URL (e.g. https://www.figma.com/file/...)"
        required: true
        type: string
      jira_ticket:
        description: "Jira ticket ID (e.g. VISTA-123)"
        required: true
        type: string
      task_description:
        description: "What the agent should generate (e.g. 'Generate the loan summary card component')"
        required: true
        type: string

jobs:
  run-ui-agent:
    runs-on: ubuntu-latest
    permissions:
      contents: write
      pull-requests: write

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install Jira MCP dependencies
        run: npm install --prefix .github/JiraMCP

      - name: Invoke UI Agent via GitHub Copilot API
        id: invoke-agent
        env:
          GH_TOKEN: ${{ secrets.COPILOT_TOKEN }}
          FIGMA_API_TOKEN: ${{ secrets.FIGMA_API_TOKEN }}
          JIRA_BASE_URL: ${{ secrets.JIRA_BASE_URL }}
          JIRA_API_TOKEN: ${{ secrets.JIRA_API_TOKEN }}
          JIRA_USER_EMAIL: ${{ secrets.JIRA_USER_EMAIL }}
          REPO: ${{ github.repository }}
        run: |
          RESPONSE=$(curl -s -X POST \
            -H "Authorization: ******" \
            -H "Content-Type: application/json" \
            -H "Accept: application/vnd.github+json" \
            -H "X-GitHub-Api-Version: 2022-11-28" \
            "https://api.github.com/repos/${REPO}/copilot/agents/ui-agent/completions" \
            -d "{
              \"messages\": [
                {
                  \"role\": \"user\",
                  \"content\": \"Figma URL: ${{ inputs.figma_url }}\nJira Ticket: ${{ inputs.jira_ticket }}\nTask: ${{ inputs.task_description }}\"
                }
              ]
            }")

          echo "API Response: $RESPONSE"
          CONTENT=$(echo "$RESPONSE" | jq -r '.choices[0].message.content // "No content returned"')
          echo "Agent output: $CONTENT"
```

---

## End-to-End Execution Flow

```
Developer triggers workflow_dispatch
  └─ Inputs: Figma URL, Jira ticket ID, task description

  → Workflow: actions/checkout
  → Workflow: npm install --prefix .github/JiraMCP
  → Workflow: curl → GitHub Copilot Agent API (ui-agent)
      └─ Agent runtime reads .github/copilot/mcp.json
          ├─ Starts Jira MCP: node .github/JiraMCP/index.js (stdio)
          │    env: JIRA_BASE_URL, JIRA_API_TOKEN, JIRA_USER_EMAIL
          └─ Connects to Figma MCP: https://mcp.figma.com/sse (HTTP)
               header: X-Figma-Token

      → Agent fetches design specs from Figma (frames, layout, tokens)
      → Agent fetches requirements from Jira (story, ACs)
      → Agent generates Angular component(s)
      → Agent creates feature branch
      → Agent raises PR with Figma + Jira references
```

---

## Implementation Checklist

- [ ] Copy Jira MCP installable files into `.github/JiraMCP/` and verify `package.json` is present
- [ ] Add `.env.example` to `.github/JiraMCP/` and add real `.env` file to `.gitignore`
- [ ] Create `.github/copilot/mcp.json` with Figma (HTTP) and Jira (stdio) entries
- [ ] Confirm Jira MCP env variable keys match what `.jira-mcp.env` defines and update `mcp.json` accordingly
- [ ] Add org-level secrets: `FIGMA_API_TOKEN`, `JIRA_BASE_URL`, `JIRA_API_TOKEN`, `JIRA_USER_EMAIL`
- [ ] Enable those secrets for Copilot agents in org settings
- [ ] Create Figma service account access token (read scopes) and store as `FIGMA_API_TOKEN`
- [ ] Create `.github/agents/ui-agent.agent.md`
- [ ] Create `.github/workflows/run-ui-agent.yaml`
- [ ] Test: trigger `run-ui-agent` workflow with a real Figma URL and Jira ticket
- [ ] Verify agent uses Figma MCP (fetches design) and Jira MCP (fetches requirements)
- [ ] Verify agent raises a PR with generated Angular component code

---

## Notes & Caveats

| Topic | Detail |
|---|---|
| **Jira MCP env keys** | Update `mcp.json` env block to exactly match the keys in your `.jira-mcp.env` file |
| **Figma MCP rate limits** | The official `mcp.figma.com` server is subject to Figma API rate limits on the token |
| **Agent model** | `claude-sonnet-5` is recommended for code generation quality; adjustable in `ui-agent.agent.md` |
| **Jira MCP transport** | `stdio` means the agent runtime spawns the Node.js process locally — the process must start cleanly with no interactive prompts |
| **Secret rotation** | Rotate `FIGMA_API_TOKEN` and `JIRA_API_TOKEN` periodically and update org secrets accordingly |