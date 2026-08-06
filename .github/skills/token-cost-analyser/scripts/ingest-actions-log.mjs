#!/usr/bin/env node
import { createReadStream, appendFileSync, mkdirSync, existsSync } from 'node:fs';
import { createInterface } from 'node:readline';
import { homedir } from 'node:os';
import { resolve, dirname } from 'node:path';
import { normalizeActionsEntry } from './normalize-event.mjs';

const MARKER = '##[lending-telemetry]';

const args = process.argv.slice(2);
const get = (flag) => { const i = args.indexOf(flag); return i !== -1 ? args[i + 1] : null; };

const logPath   = get('--log');
const sessionId = get('--session-id');
const dryRun    = args.includes('--dry-run');

if (!logPath) {
  console.error('Usage: ingest-actions-log.mjs --log <path> [--session-id <uuid>] [--dry-run]');
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
  const idx = line.indexOf(MARKER);
  if (idx === -1) continue;
  const jsonStr = line.slice(idx + MARKER.length).trim();
  let raw;
  try { raw = JSON.parse(jsonStr); } catch { skipped++; continue; }
  const event = normalizeActionsEntry(raw, sessionId);
  if (!event) { skipped++; continue; }
  const ndjson = JSON.stringify(event);
  if (dryRun) { console.log(ndjson); } else { appendFileSync(sink, ndjson + '\n'); }
  ingested++;
}

console.error(`[ingest-actions-log] ingested=${ingested} skipped=${skipped} sink=${dryRun ? 'DRY-RUN' : sink}`);
