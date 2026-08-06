#!/usr/bin/env node
import { readFileSync, writeFileSync, mkdirSync, existsSync } from 'node:fs';
import { resolve, dirname, join } from 'node:path';
import { homedir } from 'node:os';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const PRICING_PATH = resolve(__dirname, '..', 'data', 'model-pricing.json');

const args = process.argv.slice(2);
const get = (flag) => { const i = args.indexOf(flag); return i !== -1 ? args[i + 1] : null; };

const agentId    = get('--agent-id');
const modelId    = get('--model');
const inputTok   = parseInt(get('--input-tokens')  || '0', 10);
const outputTok  = parseInt(get('--output-tokens') || '0', 10);
const cacheRead  = parseInt(get('--cache-read')    || '0', 10);
const cacheWrite = parseInt(get('--cache-write')   || '0', 10);
const durationMs = parseInt(get('--duration-ms')   || '0', 10);
const eventsFile  = get('--events-file');
const reportDir   = get('--report-dir') || '.github/reports';
const filterSid   = get('--session-id');

const pricing = JSON.parse(readFileSync(PRICING_PATH, 'utf8'));

function findPrice(mid) {
  const norm = mid.replace(/\./g, '-');
  for (const provider of Object.values(pricing)) {
    if (typeof provider !== 'object' || provider._updated) continue;
    if (provider[norm]) return provider[norm];
    if (provider[mid])  return provider[mid];
  }
  for (const provider of Object.values(pricing)) {
    if (typeof provider !== 'object' || provider._updated) continue;
    for (const [key, val] of Object.entries(provider)) {
      if (key.startsWith('_')) continue;
      if (norm.startsWith(key) || key.startsWith(norm)) return val;
      if (mid.startsWith(key)  || key.startsWith(mid))  return val;
    }
  }
  return null;
}

function calcCost(tokens, price) {
  if (!price) return null;
  const M = 1_000_000;
  const inp    = (tokens.input       || 0) / M * price.input_per_1m;
  const out    = (tokens.output      || 0) / M * price.output_per_1m;
  const creadR = price.cache_read_per_1m  ?? price.input_per_1m * 0.1;
  const cwritR = price.cache_write_per_1m ?? price.input_per_1m * 1.25;
  const cread  = (tokens.cache_read  || 0) / M * creadR;
  const cwrite = (tokens.cache_write || 0) / M * cwritR;
  const total  = inp + out + cread + cwrite;
  const saved  = Math.max(0, (tokens.cache_read || 0) / M * (price.input_per_1m - creadR));
  return { input: inp, output: out, cache_read: cread, cache_write: cwrite, total, saved };
}

function money(n) {
  if (n === 0) return '$0.00';
  const s = n.toFixed(8).replace(/\.?0+$/, '');
  return '$' + s;
}

function commas(n) {
  return n.toLocaleString('en-US');
}

function buildReport(events, dateStr) {
  const lines = [];
  lines.push('=== TOKEN COST REPORT ===');
  lines.push(`DATE:   ${dateStr}`);
  lines.push(`EVENTS: ${events.length}`);

  let totalCost = 0, totalSaved = 0;

  for (const ev of events) {
    const price = findPrice(ev.model);
    const cost  = calcCost(ev.tokens, price);
    lines.push('');
    lines.push(`--- ${ev.agent_id} ---`);
    lines.push(`MODEL:  ${ev.model}`);
    lines.push(`IN:     ${commas(ev.tokens.input  || 0)} tok${cost ? ' = ' + money(cost.input)  : ''}`);
    lines.push(`OUT:    ${commas(ev.tokens.output || 0)} tok${cost ? ' = ' + money(cost.output) : ''}`);
    const cr = ev.tokens.cache_read  || 0;
    const cw = ev.tokens.cache_write || 0;
    if (cr > 0 || cw > 0) {
      const savedStr = cost ? ` (saved ${money(cost.saved)})` : '';
      lines.push(`CACHE:  ${commas(cr)} tok read${cost ? ' = ' + money(cost.cache_read) : ''}${savedStr}`);
    }
    if (cost) {
      lines.push(`COST:   ${money(cost.total)}`);
      totalCost  += cost.total;
      totalSaved += cost.saved;
    } else {
      lines.push(`COST:   unknown (model pricing not found for "${ev.model}")`);
    }
    if (ev.duration_ms) lines.push(`TIME:   ${ev.duration_ms}ms`);
  }

  lines.push('');
  lines.push('=========================');
  lines.push(`TOTAL:  ${money(totalCost)}`);
  lines.push(`SAVED:  ${money(totalSaved)}`);
  lines.push(`SRC:    ${dateStr}`);
  lines.push('=========================');

  return lines.join('\n');
}

let events = [];

if (eventsFile) {
  const raw = readFileSync(resolve(eventsFile), 'utf8');
  events = raw.split('\n').filter(Boolean).map(l => JSON.parse(l));
  if (filterSid) events = events.filter(e => e.session_id === filterSid);
} else if (agentId && modelId) {
  events = [{
    ts: new Date().toISOString(),
    session_id: 'direct',
    agent_id: agentId,
    model: modelId,
    tokens: { input: inputTok, output: outputTok, cache_read: cacheRead, cache_write: cacheWrite },
    trigger: 'direct-cli',
    ...(durationMs ? { duration_ms: durationMs } : {}),
  }];
} else {
  console.error(
    'Usage:\n' +
    '  # Direct mode\n' +
    '  generate-cost-report.mjs --agent-id <id> --model <id> --input-tokens <N> --output-tokens <N> [--cache-read <N>] [--cache-write <N>] [--duration-ms <N>] [--report-dir <dir>]\n' +
    '  # NDJSON mode\n' +
    '  generate-cost-report.mjs --events-file <path> [--report-dir <dir>]'
  );
  process.exit(1);
}

const dateStr    = new Date().toISOString().slice(0, 10);
const report     = buildReport(events, dateStr);

const absReportDir = resolve(reportDir);
if (!existsSync(absReportDir)) mkdirSync(absReportDir, { recursive: true });

const agentSlug = (agentId || events[0]?.agent_id || 'unknown').replace(/[^a-z0-9-]/gi, '-');
const ts        = new Date().toISOString().replace(/[:.]/g, '-');
const filename  = `cost-report-${ts}-${agentSlug}.txt`;
const outPath   = join(absReportDir, filename);

writeFileSync(outPath, report, 'utf8');
console.log(`[token-cost-analyser] report → ${outPath}`);
