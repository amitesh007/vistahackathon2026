import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import axios, { AxiosInstance, AxiosError } from "axios";
import { z } from "zod";
import fs from "fs/promises";
import path from "path";

// ── Config & Jira client ────────────────────────────────────────────────────
// JIRA_AUTH_TYPE: "basic" (Cloud: email + API token) | "bearer" (Server/DC: PAT)
// JIRA_API_VERSION: "3" (Cloud default) | "2" (Server/DC default)

const JIRA_URL = process.env.JIRA_URL?.replace(/\/$/, "");
const JIRA_EMAIL = process.env.JIRA_EMAIL;
const JIRA_API_TOKEN = process.env.JIRA_API_TOKEN;
const JIRA_AUTH_TYPE = (process.env.JIRA_AUTH_TYPE ?? "basic").toLowerCase();
const JIRA_API_VERSION = process.env.JIRA_API_VERSION ?? (JIRA_AUTH_TYPE === "bearer" ? "2" : "3");

if (!JIRA_URL || !JIRA_API_TOKEN) {
  console.error("Missing required env vars: JIRA_URL, JIRA_API_TOKEN");
  process.exit(1);
}
if (JIRA_AUTH_TYPE === "basic" && !JIRA_EMAIL) {
  console.error("JIRA_EMAIL is required when JIRA_AUTH_TYPE=basic");
  process.exit(1);
}

const jira: AxiosInstance = axios.create({
  baseURL: `${JIRA_URL}/rest/api/${JIRA_API_VERSION}`,
  ...(JIRA_AUTH_TYPE === "basic"
    ? { auth: { username: JIRA_EMAIL!, password: JIRA_API_TOKEN } }
    : {}),
  headers: {
    Accept: "application/json",
    "Content-Type": "application/json",
    ...(JIRA_AUTH_TYPE === "bearer"
      ? { Authorization: `Bearer ${JIRA_API_TOKEN}` }
      : {}),
  },
});

// Separate client for binary attachment downloads — no baseURL, arraybuffer response
const downloader: AxiosInstance = axios.create({
  ...(JIRA_AUTH_TYPE === "basic"
    ? { auth: { username: JIRA_EMAIL!, password: JIRA_API_TOKEN } }
    : {}),
  headers: {
    ...(JIRA_AUTH_TYPE === "bearer"
      ? { Authorization: `Bearer ${JIRA_API_TOKEN}` }
      : {}),
  },
  responseType: "arraybuffer",
  maxContentLength: 100 * 1024 * 1024, // 100 MB
  maxBodyLength: 100 * 1024 * 1024,
});

// Default assets dir: <cwd>/assets — overridable via ASSETS_DIR env var
const ASSETS_DIR = process.env.ASSETS_DIR ?? path.join(process.cwd(), "assets");

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

// Extract plain text from either a v2 HTML/wiki string or a v3 ADF document
function extractText(desc: unknown): string {
  if (!desc) return "(no description)";
  if (typeof desc === "string") {
    // Strip HTML tags and decode common entities
    return desc
      .replace(/<br\s*\/?>/gi, "\n")
      .replace(/<\/p>/gi, "\n")
      .replace(/<[^>]+>/g, "")
      .replace(/&amp;/g, "&")
      .replace(/&lt;/g, "<")
      .replace(/&gt;/g, ">")
      .replace(/&nbsp;/g, " ")
      .replace(/\n{3,}/g, "\n\n")
      .trim();
  }
  const walk = (node: Record<string, unknown>): string => {
    if (node.type === "text") return String(node.text ?? "");
    if (node.type === "hardBreak") return "\n";
    const children = Array.isArray(node.content)
      ? (node.content as Record<string, unknown>[]).map(walk).join("")
      : "";
    return node.type === "paragraph" || node.type === "heading" ? children + "\n" : children;
  };
  return walk(desc as Record<string, unknown>).trim();
}

// Build comment body — plain string for v2, ADF doc for v3
function commentBody(text: string): unknown {
  if (JIRA_API_VERSION === "2") return text;
  return { type: "doc", version: 1, content: [{ type: "paragraph", content: [{ type: "text", text }] }] };
}

function jiraError(err: unknown): string {
  if (err instanceof AxiosError) {
    const msg =
      err.response?.data?.errorMessages?.join(", ") ||
      err.response?.data?.message ||
      err.message;
    return `Jira API error (${err.response?.status ?? "unknown"}): ${msg}`;
  }
  return String(err);
}

// ── MCP server ──────────────────────────────────────────────────────────────

const server = new McpServer({
  name: "jira-mcp-server",
  version: "1.0.0",
});

// ── Tool: jira_get_issue ────────────────────────────────────────────────────

server.tool(
  "jira_get_issue",
  "Fetch a single Jira issue by its key (e.g. PROJ-123).",
  { issueKey: z.string().describe("Jira issue key, e.g. PROJ-123") },
  async ({ issueKey }) => {
    try {
      const { data } = await jira.get(`/issue/${issueKey}`, {
        params: {
          fields:
            "summary,status,assignee,reporter,priority,issuetype,description,comment,labels,created,updated",
        },
      });

      const f = data.fields;
      const comments: string = (f.comment?.comments ?? [])
        .slice(-3)
        .map(
          (c: Record<string, unknown>) =>
            `  [${(c.author as Record<string, string>)?.displayName}]: ${extractText(c.body)}`
        )
        .join("\n");

      return {
        content: [
          {
            type: "text",
            text: [
              `Key:       ${data.key}`,
              `Summary:   ${f.summary}`,
              `Type:      ${f.issuetype?.name}`,
              `Status:    ${f.status?.name}`,
              `Priority:  ${f.priority?.name}`,
              `Assignee:  ${f.assignee?.displayName ?? "Unassigned"}`,
              `Reporter:  ${f.reporter?.displayName}`,
              `Labels:    ${(f.labels ?? []).join(", ") || "none"}`,
              `Created:   ${f.created}`,
              `Updated:   ${f.updated}`,
              `Description:\n${extractText(f.description)}`,
              `URL:       ${JIRA_URL}/browse/${data.key}`,
              comments ? `\nLast comments:\n${comments}` : "",
            ]
              .filter(Boolean)
              .join("\n"),
          },
        ],
      };
    } catch (err) {
      return { content: [{ type: "text", text: jiraError(err) }], isError: true };
    }
  }
);

// ── Tool: jira_search_issues ────────────────────────────────────────────────

server.tool(
  "jira_search_issues",
  "Search Jira issues using a JQL query.",
  {
    jql: z.string().describe('JQL query, e.g. project = PROJ AND status = "In Progress"'),
    maxResults: z
      .number()
      .min(1)
      .max(50)
      .default(10)
      .describe("Maximum results to return (1-50, default 10)"),
  },
  async ({ jql, maxResults }) => {
    try {
      const { data } = await jira.post("/search", {
        jql,
        maxResults,
        fields: ["summary", "status", "assignee", "priority", "issuetype", "updated"],
      });

      if (data.issues.length === 0) {
        return { content: [{ type: "text", text: "No issues found." }] };
      }

      const rows = data.issues.map(
        (i: Record<string, unknown>) => {
          const f = i.fields as Record<string, Record<string, string>>;
          return `- ${i.key}: [${f.issuetype?.name}] ${f.summary} | ${f.status?.name} | ${f.assignee?.displayName ?? "Unassigned"} | ${f.priority?.name}`;
        }
      );

      return {
        content: [
          {
            type: "text",
            text: `Found ${data.total} issue(s), showing ${data.issues.length}:\n\n${rows.join("\n")}`,
          },
        ],
      };
    } catch (err) {
      return { content: [{ type: "text", text: jiraError(err) }], isError: true };
    }
  }
);

// ── Tool: jira_list_projects ────────────────────────────────────────────────

server.tool(
  "jira_list_projects",
  "List all Jira projects accessible to the configured account.",
  {},
  async () => {
    try {
      const { data } = await jira.get("/project", {
        params: { expand: "lead" },
      });

      const rows = (data as Record<string, string>[]).map(
        (p) => `- ${p.key}: ${p.name} (lead: ${(p.lead as unknown as Record<string, string>)?.displayName ?? "n/a"})`
      );

      return {
        content: [{ type: "text", text: rows.join("\n") || "No projects found." }],
      };
    } catch (err) {
      return { content: [{ type: "text", text: jiraError(err) }], isError: true };
    }
  }
);

// ── Tool: jira_create_issue ─────────────────────────────────────────────────

server.tool(
  "jira_create_issue",
  "Create a new Jira issue.",
  {
    projectKey: z.string().describe("Project key, e.g. PROJ"),
    summary: z.string().describe("Issue summary / title"),
    issueType: z
      .string()
      .default("Task")
      .describe("Issue type name, e.g. Bug, Task, Story"),
    description: z.string().optional().describe("Plain-text description"),
    priority: z.string().optional().describe("Priority name, e.g. High, Medium, Low"),
    assigneeAccountId: z.string().optional().describe("Assignee account ID"),
    labels: z.array(z.string()).optional().describe("List of label strings"),
  },
  async ({ projectKey, summary, issueType, description, priority, assigneeAccountId, labels }) => {
    try {
      const fields: Record<string, unknown> = {
        project: { key: projectKey },
        summary,
        issuetype: { name: issueType },
      };

      if (description) {
        fields.description = JIRA_API_VERSION === "2"
          ? description
          : { type: "doc", version: 1, content: [{ type: "paragraph", content: [{ type: "text", text: description }] }] };
      }
      if (priority) fields.priority = { name: priority };
      if (assigneeAccountId) fields.assignee = { accountId: assigneeAccountId };
      if (labels?.length) fields.labels = labels;

      const { data } = await jira.post("/issue", { fields });

      return {
        content: [
          {
            type: "text",
            text: `Created issue ${data.key}\nURL: ${JIRA_URL}/browse/${data.key}`,
          },
        ],
      };
    } catch (err) {
      return { content: [{ type: "text", text: jiraError(err) }], isError: true };
    }
  }
);

// ── Tool: jira_add_comment ──────────────────────────────────────────────────

server.tool(
  "jira_add_comment",
  "Add a comment to an existing Jira issue.",
  {
    issueKey: z.string().describe("Jira issue key"),
    comment: z.string().describe("Comment body text"),
  },
  async ({ issueKey, comment }) => {
    try {
      await jira.post(`/issue/${issueKey}/comment`, { body: commentBody(comment) });

      return {
        content: [{ type: "text", text: `Comment added to ${issueKey}.` }],
      };
    } catch (err) {
      return { content: [{ type: "text", text: jiraError(err) }], isError: true };
    }
  }
);

// ── Tool: jira_get_transitions ──────────────────────────────────────────────

server.tool(
  "jira_get_transitions",
  "List available workflow transitions for a Jira issue.",
  { issueKey: z.string().describe("Jira issue key") },
  async ({ issueKey }) => {
    try {
      const { data } = await jira.get(`/issue/${issueKey}/transitions`);
      const rows = data.transitions.map(
        (t: Record<string, string>) => `- ID ${t.id}: ${t.name} → ${(t.to as unknown as Record<string, string>)?.name}`
      );
      return {
        content: [{ type: "text", text: rows.join("\n") || "No transitions available." }],
      };
    } catch (err) {
      return { content: [{ type: "text", text: jiraError(err) }], isError: true };
    }
  }
);

// ── Tool: jira_transition_issue ─────────────────────────────────────────────

server.tool(
  "jira_transition_issue",
  "Move a Jira issue to a new status via a workflow transition.",
  {
    issueKey: z.string().describe("Jira issue key"),
    transitionId: z.string().describe("Transition ID from jira_get_transitions"),
  },
  async ({ issueKey, transitionId }) => {
    try {
      await jira.post(`/issue/${issueKey}/transitions`, {
        transition: { id: transitionId },
      });
      return {
        content: [{ type: "text", text: `Transitioned ${issueKey} using transition ${transitionId}.` }],
      };
    } catch (err) {
      return { content: [{ type: "text", text: jiraError(err) }], isError: true };
    }
  }
);

// ── Tool: jira_download_attachments ─────────────────────────────────────────

server.tool(
  "jira_download_attachments",
  "Download all file attachments from a Jira issue to a local assets folder.",
  {
    issueKey: z.string().describe("Jira issue key, e.g. PROJ-123"),
    targetDir: z.string().optional().describe(
      `Override save directory (default: assets/<ISSUE-KEY>/ relative to server cwd)`
    ),
  },
  async ({ issueKey, targetDir }) => {
    try {
      const { data } = await jira.get(`/issue/${issueKey}`, {
        params: { fields: "attachment,summary" },
      });

      const attachments: Record<string, unknown>[] = data.fields?.attachment ?? [];

      if (attachments.length === 0) {
        return { content: [{ type: "text", text: `No attachments found on ${issueKey}.` }] };
      }

      const saveDir = targetDir ?? path.join(ASSETS_DIR, issueKey);
      await fs.mkdir(saveDir, { recursive: true });

      const lines: string[] = [
        `Found ${attachments.length} attachment(s) on ${issueKey}. Downloading to: ${saveDir}\n`,
      ];

      let successCount = 0;
      for (const att of attachments) {
        const filename = String(att.filename ?? "unknown");
        const contentUrl = String(att.content ?? "");
        const size = Number(att.size ?? 0);
        const mimeType = String(att.mimeType ?? "application/octet-stream");

        if (!contentUrl) {
          lines.push(`  ✗ ${filename} — no content URL`);
          continue;
        }

        try {
          const response = await downloader.get(contentUrl);
          const filePath = path.join(saveDir, filename);
          await fs.writeFile(filePath, Buffer.from(response.data as ArrayBuffer));
          lines.push(`  ✓ ${filename} (${formatSize(size)}, ${mimeType}) → ${filePath}`);
          successCount++;
        } catch (downloadErr) {
          const msg = downloadErr instanceof AxiosError
            ? `HTTP ${downloadErr.response?.status ?? "?"}: ${downloadErr.message}`
            : String(downloadErr);
          lines.push(`  ✗ ${filename} — ${msg}`);
        }
      }

      lines.push(`\nResult: ${successCount}/${attachments.length} downloaded successfully.`);

      return { content: [{ type: "text", text: lines.join("\n") }] };
    } catch (err) {
      return { content: [{ type: "text", text: jiraError(err) }], isError: true };
    }
  }
);

// ── Start ───────────────────────────────────────────────────────────────────

const transport = new StdioServerTransport();
await server.connect(transport);
console.error("Jira MCP server running on stdio");
