#!/usr/bin/env node
/**
 * normalize-event.mjs
 * Lending BU — Token Meter Skill
 *
 * Converts a raw Copilot debug log entry OR a GitHub Actions runner log entry
 * into the canonical token-event schema. Called by the ingestor scripts.
 *
 * Usage (module — not invoked directly):
 *   import { normalizeVSCodeEntry, normalizeActionsEntry } from './normalize-event.mjs';
 *
 * Each normalizer returns a token-event object or null (if the entry is not
 * a token-usage record).
 */

/**
 * Normalize a single parsed VS Code Copilot debug log entry.
 * The debug log emits JSON objects on each line when logLevel >= debug.
 */
export function normalizeVSCodeEntry(raw, sessionId, agentIdOverride) {
  if (!raw || typeof raw !== 'object') return null;

  // Current Copilot debug log shape (v0.55.x+): flat trace-span records.
  // { v, ts, dur, sid, type: "llm_request", spanId, attrs: { model, inputTokens, outputTokens, cachedTokens, ... } }
  // NOTE: attrs also carries `userRequest` / `inputMessages` / `response` fields containing prompt/code
  // text on some event types — these are NEVER read here per the no-prompt-content guardrail.
  if (raw.type === 'llm_request' && raw.attrs && (raw.attrs.inputTokens != null || raw.attrs.outputTokens != null)) {
    const attrs = raw.attrs;
    return {
      ts:           typeof raw.ts === 'number' ? new Date(raw.ts).toISOString() : (raw.ts || new Date().toISOString()),
      session_id:   sessionId || raw.sid || 'unknown',
      agent_id:     agentIdOverride || attrs.debugName || 'unknown',
      step_id:      raw.spanId ?? null,
      skill_id:     null,
      model:        attrs.model || 'unknown',
      tokens: {
        input:       parseInt(attrs.inputTokens  || 0, 10),
        output:      parseInt(attrs.outputTokens || 0, 10),
        cache_read:  parseInt(attrs.cachedTokens || 0, 10),
        cache_write: 0,
      },
      wall_clock_ms: attrs.ttft ?? raw.dur ?? null,
      repo:          raw.repo || null,
      actor:         raw.actor || process.env.USER || null,
      trigger:       'vscode-chat',
    };
  }

  // Legacy/alternate Copilot debug log shape (approximate — varies by version):
  // { type: 'chat.response', model: '...', usage: { prompt_tokens, completion_tokens }, timestamp: '...' }
  if (!raw.usage && !raw.tokens) return null;

  const usage = raw.usage || raw.tokens || {};
  return {
    ts:           raw.timestamp || new Date().toISOString(),
    session_id:   sessionId || raw.sessionId || 'unknown',
    agent_id:     agentIdOverride || raw.agentId || raw.agent_id || 'unknown',
    step_id:      raw.stepId ?? raw.step_id ?? null,
    skill_id:     raw.skillId ?? raw.skill_id ?? null,
    model:        raw.model || raw.modelId || 'unknown',
    tokens: {
      input:       parseInt(usage.prompt_tokens   || usage.input   || 0, 10),
      output:      parseInt(usage.completion_tokens || usage.output || 0, 10),
      cache_read:  parseInt(usage.cache_read  || 0, 10),
      cache_write: parseInt(usage.cache_write || 0, 10),
    },
    wall_clock_ms: raw.durationMs ?? raw.wall_clock_ms ?? null,
    repo:          raw.repo || null,
    actor:         raw.actor || process.env.USER || null,
    trigger:       'vscode-chat',
  };
}

/**
 * Normalize a single GitHub Actions runner log line.
 * Runner logs that include token usage have a structured JSON payload
 * emitted by the lending-token-meter hook after each agent step.
 */
export function normalizeActionsEntry(raw, sessionId) {
  if (!raw || typeof raw !== 'object') return null;
  if (!raw.tokens && !raw.usage) return null;

  const usage = raw.tokens || raw.usage || {};
  return {
    ts:           raw.ts || raw.timestamp || new Date().toISOString(),
    session_id:   sessionId || raw.session_id || raw.sessionId || 'unknown',
    agent_id:     raw.agent_id || raw.agentId || 'unknown',
    step_id:      raw.step_id ?? raw.stepId ?? null,
    skill_id:     raw.skill_id ?? raw.skillId ?? null,
    model:        raw.model || 'unknown',
    tokens: {
      input:       parseInt(usage.input  || usage.prompt_tokens      || 0, 10),
      output:      parseInt(usage.output || usage.completion_tokens  || 0, 10),
      cache_read:  parseInt(usage.cache_read  || 0, 10),
      cache_write: parseInt(usage.cache_write || 0, 10),
    },
    wall_clock_ms: raw.wall_clock_ms ?? null,
    repo:          raw.repo || process.env.GITHUB_REPOSITORY || null,
    actor:         raw.actor || process.env.GITHUB_ACTOR || null,
    trigger:       'actions-workflow',
  };
}
