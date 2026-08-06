#!/usr/bin/env node
/**
 * ingest-actions-log.mjs
 * Lending BU — Token Meter Skill
 *
 * Reads a GitHub Actions runner log file and appends normalized token-event
 * records. The runner log may contain JSON payloads on lines prefixed with
 * the lending telemetry marker.
 *
 * Usage:
 *   node ingest-actions-log.mjs --log <path>
 *   node ingest-actions-log.mjs --log <path> --session-id <uuid> --dry-run
 *
 * Marker format expected in Actions log:
 *   ##[lending-telemetry] {"agent_id":"...","tokens":{...},...}
 *
 * Sink: $LENDING_TELEMETRY_SINK (default: ~/.lending-telemetry/token-events.ndjson)
 * Output (JSON to stdout): { ingested, skipped, sink }
 */

import { readFileSync, appendFileSync, existsSync, mkdirSync } from 'fs';
import { resolve, dirname } from 'path';
import { homedir } from 'os';
import { join } from 'path';
import { normalizeActionsEntry } from './normalize-event.mjs';

const args = process.argv.slice(2);
const getArg = (f) => { const i = args.indexOf(f); return i !== -1 ? args[i + 1] : null; };
const hasFlag = (f) => args.includes(f);

const logPath   = getArg('--log');
const sessionId = getArg('--session-id') || process.env.GITHUB_RUN_ID || 'unknown';
const dryRun    = hasFlag('--dry-run');
const sinkArg   = process.env.LENDING_TELEMETRY_SINK ||
                  join(homedir(), '.lending-telemetry', 'token-events.ndjson');

const MARKER = '##[lending-telemetry]';

if (!logPath) {
  process.stderr.write('ERROR: --log <path> is required\n'); process.exit(1);
}
if (!existsSync(resolve(logPath))) {
  // Missing log file is normal (no Actions run recorded yet).
  process.stdout.write(JSON.stringify({ ingested: 0, skipped: 0, sink: dryRun ? '(dry-run)' : sinkArg, note: 'log file not found' }, null, 2) + '\n');
  process.exit(0);
}

const lines = readFileSync(resolve(logPath), 'utf8').split('\n').filter(Boolean);
let ingested = 0;
let skipped  = 0;
const events = [];

for (const line of lines) {
  const idx = line.indexOf(MARKER);
  if (idx === -1) { skipped++; continue; }
  const payload = line.slice(idx + MARKER.length).trim();
  let raw;
  try { raw = JSON.parse(payload); } catch { skipped++; continue; }
  const event = normalizeActionsEntry(raw, sessionId);
  if (!event) { skipped++; continue; }
  events.push(JSON.stringify(event));
  ingested++;
}

if (!dryRun && events.length > 0) {
  if (sinkArg.startsWith('s3://')) {
    process.stderr.write(`WARN: S3 sink not supported by ingest script — writing to local fallback\n`);
    const local = join(homedir(), '.lending-telemetry', 'token-events.ndjson');
    mkdirSync(dirname(local), { recursive: true });
    appendFileSync(local, events.join('\n') + '\n', 'utf8');
  } else {
    mkdirSync(dirname(resolve(sinkArg)), { recursive: true });
    appendFileSync(resolve(sinkArg), events.join('\n') + '\n', 'utf8');
  }
}

process.stdout.write(JSON.stringify({ ingested, skipped, sink: dryRun ? '(dry-run)' : sinkArg }, null, 2) + '\n');
