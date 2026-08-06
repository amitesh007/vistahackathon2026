AGENT ORCHESTRATION ARCHITECTURE
==================================

## Overview
The agents system is a TypeScript/Node.js multi-agent orchestration framework.
Location: vistahackathon26/agents/

## Core Concepts

### IAgent Interface (src/core/agent.interface.ts)
Every agent implements IAgent<TInput, TOutput>:
- id: string — unique identifier used in registry and pipeline steps
- name: string — human-readable label for logging
- description: string — what the agent does
- execute(input, context): Promise<AgentResult<TOutput>> — main entry point
- isReady?(): Promise<boolean> — optional health check

### AgentResult (src/core/agent-result.ts)
Wrapper returned by all agent executions:
- success: boolean
- data?: T — passed to next pipeline step
- error?: string — set when success === false
- meta?: Record — timing, token usage, etc.

### AgentContext (src/core/agent-context.ts)
Shared mutable context threaded through all pipeline steps:
- state: Map<string, unknown> — key-value store agents read/write
- knowledge: GraphRagService — access to GraphRAG knowledge base
- runId: string — unique ID for this pipeline run
- get(key): T | undefined
- set(key, value): void
- snapshot(): Record — serialize state for logging

### BaseAgent (src/core/base.agent.ts)
Abstract class all concrete agents extend:
- Subclasses implement protected run(input, context): Promise<TOutput>
- execute() wraps run() with logging and try/catch
- Default isReady() returns true (override for real health checks)

### AgentRegistry (src/registry/agent-registry.ts)
Central registry of all agents:
- register(agent): this — chainable
- resolve<T>(id): T — throws if not found
- list(): IAgent[] — all registered agents
- has(id): boolean

### Pipeline Model (src/pipeline/pipeline.model.ts)
Pipeline = ordered array of PipelineSteps:
- agentId: string — references registered agent
- label?: string — log label
- config?: Record — static config merged into input
- continueOnError?: boolean — default false

### PipelineRunner (src/pipeline/pipeline.runner.ts)
Executes pipeline sequentially:
- Output of step N becomes input of step N+1
- Aborts on first failure (unless continueOnError)
- Returns PipelineRunResult with per-step outcomes

### Orchestrator (src/orchestrator/orchestrator.ts)
Host orchestrator — single entry point:
- Owns AgentRegistry and PipelineRunner
- Creates AgentContext with GraphRagService for each run
- Validates all agentIds before running
- run(pipeline, initialInput, initialState): Promise<PipelineRunResult>
- listAgents(): agent summaries

## Registered Agents

### JiraReaderAgent (id: 'jira-reader')
Input: { issueKey, jiraBaseUrl? }
Output: { issueKey, title, description, acceptanceCriteria, labels, specFilePath }
Steps:
1. Fetch Jira issue via REST API (Basic auth with JIRA_EMAIL + JIRA_API_TOKEN)
2. Query GraphRAG knowledge base for relevant coding conventions
3. Build structured markdown spec file
4. Write spec to ./specs/<issueKey>-spec.md
5. Store specFilePath and issueTitle in context

### CodeGeneratorAgent (id: 'code-generator')
Input: JiraReaderOutput (passed from previous step)
Output: { issueKey, generatedFiles, branchName, prUrl? }
Steps:
1. Read spec file from disk
2. Query GraphRAG for Angular code patterns
3. Generate 4 Angular files: .component.ts, .html, .scss, .spec.ts
4. Write to ./generated/<issueKey>/
5. Create GitHub PR via GitHub REST API (needs GITHUB_TOKEN, GITHUB_REPO)

## Environment Variables
JIRA_BASE_URL=https://yourcompany.atlassian.net
JIRA_EMAIL=user@company.com
JIRA_API_TOKEN=<token>
GITHUB_TOKEN=<personal-access-token>
GITHUB_REPO=owner/repo
GITHUB_BASE_BRANCH=main
JIRA_ISSUE_KEY=PROJ-123  (or pass as CLI arg)

## Adding a New Agent
1. Create class in src/agents/<name>/<name>.agent.ts extending BaseAgent
2. Register in src/bootstrap.ts: orchestrator.registry.register(new MyAgent())
3. Export from src/index.ts
4. Create a pipeline step referencing the new agentId

## Pipeline: jira-to-pr
Defined in: src/pipelines/jira-to-pr.pipeline.ts
Step 1: jira-reader — fetches Jira, writes spec
Step 2: code-generator — reads spec, generates code, opens PR

## Running
npm run dev PROJ-123   (via ts-node)
npm run build && npm start PROJ-123   (compiled)
