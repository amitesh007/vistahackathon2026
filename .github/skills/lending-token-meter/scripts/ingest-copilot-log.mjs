#!/usr/bin/env node
/**
 * ingest-copilot-log.mjs
 * Lending BU — Token Meter Skill
 *
 * Reads VS Code Copilot debug log (NDJSON) and appends normalized
 * token-event records to the configured sink.
 *
 * Usage:
 *   node ingest-copilot-log.mjs --log <path-to-copilot-debug.log>
 *   node ingest-copilot-log.mjs --log <path> --session-id <uuid>
 *   node ingest-copilot-log.mjs --log <path> --dry-run
 *
 * Sink: $LENDING_TELEMETRY_SINK (file path or s3:// URL)
 *       default: ~/.lending-telemetry/token-events.ndjson
 *
 * Output (JSON to stdout): { ingested, skipped, sink }
 */

import { readFileSync, appendFileSync, existsSync, mkdirSync } from 'fs';
import { join, resolve, dirname } from 'path';
import { homedir } from 'os';
import { normalizeVSCodeEntry } from './normalize-event.mjs';

const args = process.argv.slice(2);
const getArg = (f) => { const i = args.indexOf(f); return i !== -1 ? args[i + 1] : null; };
const hasFlag = (f) => args.includes(f);

const logPath   = getArg('--log');
const sessionId = getArg('--session-id') || 'unknown';
const dryRun    = hasFlag('--dry-run');
const sinkArg   = process.env.LENDING_TELEMETRY_SINK ||
                  join(homedir(), '.lending-telemetry', 'token-events.ndjson');

if (!logPath) {
  process.stderr.write('ERROR: --log <path> is required\n'); process.exit(1);
}
if (!existsSync(resolve(logPath))) {
  // Missing log file is normal (no Copilot session recorded yet).
  process.stdout.write(JSON.stringify({ ingested: 0, skipped: 0, sink: dryRun ? '(dry-run)' : sinkArg, note: 'log file not found' }, null, 2) + '\n');
  process.exit(0);
}

const lines = readFileSync(resolve(logPath), 'utf8').split('\n').filter(Boolean);
let ingested = 0;
let skipped  = 0;
const events = [];

for (const line of lines) {
  let raw;
  try { raw = JSON.parse(line); } catch { skipped++; continue; }
  const event = normalizeVSCodeEntry(raw, sessionId);
  if (!event) { skipped++; continue; }
  events.push(JSON.stringify(event));
  ingested++;
}

if (!dryRun && events.length > 0) {
  // Remote S3 sink — not implemented in this script; use a separate uploader
  if (sinkArg.startsWith('s3://')) {
    process.stderr.write(`WARN: S3 sink not supported by this script — events written to local fallback\n`);
    const local = join(homedir(), '.lending-telemetry', 'token-events.ndjson');
    mkdirSync(dirname(local), { recursive: true });
    appendFileSync(local, events.join('\n') + '\n', 'utf8');
  } else {
    mkdirSync(dirname(resolve(sinkArg)), { recursive: true });
    appendFileSync(resolve(sinkArg), events.join('\n') + '\n', 'utf8');
  }
}

process.stdout.write(JSON.stringify({ ingested, skipped, sink: dryRun ? '(dry-run)' : sinkArg }, null, 2) + '\n');
