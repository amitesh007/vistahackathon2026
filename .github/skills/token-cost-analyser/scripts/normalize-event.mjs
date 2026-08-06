import { randomUUID } from 'node:crypto';

const SKILL_ID = 'token-cost-analyser';

export function normalizeVSCodeEntry(raw, sessionId, agentIdOverride) {
  let tokens = null;
  let model = raw.model || raw.modelId || 'unknown';
  let durationMs;

  if (raw.type === 'llm_request' && raw.attrs) {
    const a = raw.attrs;
    const input  = a.input_tokens  ?? a.prompt_tokens  ?? a.inputTokens  ?? 0;
    const output = a.output_tokens ?? a.completion_tokens ?? a.outputTokens ?? 0;
    if (input || output) {
      tokens = {
        input,
        output,
        ...(a.cache_read_input_tokens != null ? { cache_read: a.cache_read_input_tokens }
          : a.cachedTokens != null             ? { cache_read: a.cachedTokens }
          : {}),
        ...(a.cache_creation_input_tokens != null ? { cache_write: a.cache_creation_input_tokens } : {}),
      };
    }
    model = a.model || model;
    durationMs = a.duration_ms ?? a.durationMs ?? a.ttft;
  } else if (raw.usage || raw.tokens) {
    const u = raw.usage || raw.tokens;
    const input = u.input_tokens ?? u.prompt_tokens ?? 0;
    const output = u.output_tokens ?? u.completion_tokens ?? 0;
    if (input || output) {
      tokens = {
        input,
        output,
        ...(u.cache_read_input_tokens != null ? { cache_read: u.cache_read_input_tokens } : {}),
        ...(u.cache_creation_input_tokens != null ? { cache_write: u.cache_creation_input_tokens } : {}),
      };
    }
  }

  if (!tokens) return null;

  return {
    ts: raw.ts || raw.timestamp || new Date().toISOString(),
    session_id: sessionId || randomUUID(),
    agent_id: agentIdOverride || raw.agentId || raw.agent_id || SKILL_ID,
    model,
    tokens,
    trigger: 'vscode-copilot-log',
    ...(durationMs != null ? { duration_ms: durationMs } : {}),
  };
}

export function normalizeActionsEntry(raw, sessionId) {
  const u = raw.usage || raw.tokens || {};
  const input = u.input_tokens ?? u.prompt_tokens ?? 0;
  const output = u.output_tokens ?? u.completion_tokens ?? 0;
  if (!input && !output) return null;

  return {
    ts: raw.ts || raw.timestamp || new Date().toISOString(),
    session_id: sessionId || randomUUID(),
    agent_id: raw.agent_id || raw.agentId || SKILL_ID,
    model: raw.model || raw.modelId || 'unknown',
    tokens: {
      input,
      output,
      ...(u.cache_read_input_tokens != null ? { cache_read: u.cache_read_input_tokens } : {}),
      ...(u.cache_creation_input_tokens != null ? { cache_write: u.cache_creation_input_tokens } : {}),
    },
    trigger: 'actions-runner-log',
  };
}
