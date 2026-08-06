#!/usr/bin/env node
import { createReadStream, appendFileSync, mkdirSync, existsSync } from 'node:fs';
import { createInterface } from 'node:readline';
import { homedir } from 'node:os';
import { resolve, dirname } from 'node:path';
import { normalizeVSCodeEntry } from './normalize-event.mjs';

const args = process.argv.slice(2);
const get = (flag) => { const i = args.indexOf(flag); return i !== -1 ? args[i + 1] : null; };

const logPath   = get('--log');
const sessionId = get('--session-id');
const agentId   = get('--agent-id');
const dryRun    = args.includes('--dry-run');

if (!logPath) {
  console.error('Usage: ingest-copilot-log.mjs --log <path> [--session-id <uuid>] [--agent-id <id>] [--dry-run]');
  process.exit(1);
}

const sink = process.env.LENDING_TELEMETRY_SINK
  || resolve(homedir(), '.lending-telemetry', 'token-events.ndjson');

if (!dryRun) {
  const sinkDir = dirname(sink);
  if (!existsSync(sinkDir)) mkdirSync(sinkDir, { recursive: true });
}

const rl = createInterface({ input: createReadStream(resolve(logPath)), crlfDelay: Infinity });
let ingested = 0, skipped = 0;

for await (const line of rl) {
  const trimmed = line.trim();
  if (!trimmed) continue;
  let raw;
  try { raw = JSON.parse(trimmed); } catch { skipped++; continue; }
  const event = normalizeVSCodeEntry(raw, sessionId, agentId);
  if (!event) { skipped++; continue; }
  const ndjson = JSON.stringify(event);
  if (dryRun) { console.log(ndjson); } else { appendFileSync(sink, ndjson + '\n'); }
  ingested++;
}

console.error(`[ingest-copilot-log] ingested=${ingested} skipped=${skipped} sink=${dryRun ? 'DRY-RUN' : sink}`);
