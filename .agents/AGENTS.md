# AGENTS.md

## Mediadb Plugin Guide

### Purpose

`plugins/mediadb` implements EME's JSON REST API surface and its LLM/AI provider integrations. It
has no `code/` (Java) folder of its own — endpoints are wired through `.xconf` `path-action`
entries that call into Java modules provided by other plugins (mainly `system` and `finder`), and
AI provider config lives as data under `html/ai`.

### Folder Map

- `html/services/<module>/` One folder per API area (authentication, entity, module, server,
  cluster, distribution, push, publishingdestination, settings, workspaces, whatsapp, ...); each
  action is a `<name>.xconf` + `<name>.json` (response template) pair
- `html/services/wadl.xml` / `wadl.xconf` WADL description of the REST API surface
- `html/ai/<provider>/` Per-provider AI integration config: `default`, `openai`, `gemini`, `ollama`,
  `llama`, `llamavision`, `http`, `mcp` — prompts/calls/system messages per provider
- `html/ai/<provider>/calls/*.json` One Velocity-templated LLM request body per function. The
  `.json` extension is mapped to `velocityGenerator` in `html/ai/_site.xconf`, so these are real
  Velocity templates, not static JSON — rendered against the `AgentContext`
  (`BaseLlmConnection.loadInputFromTemplate` in `plugins/finder`) and the *output* is then parsed as
  JSON. See "Editing Rules" below for the `#jesc` quoting rule before touching one of these, and the
  `create-java-ai-skill` skill (`.agents/skills/create-java-ai-skill/SKILL.md`, repo root) for the
  full pattern
- `html/mcp.old/` Legacy MCP (Model Context Protocol) client/server files — not the current
  integration path, avoid extending unless explicitly asked to
- `html/docs/` API documentation viewer pages
- `html/media/services/` Media-specific service handlers
- `html/samples/` Example client code (jQuery, static) for calling the API

### What This Plugin Owns

- The HTTP/JSON contract for the whole system: every `/services/**.json` endpoint
- AI provider wiring: which LLM backend handles a given call and how prompts are assembled
- API documentation and sample requests (paired with endpoint docs in
  `plugins/catalog/html/data/lists/endpoint/*.xml`)

### Editing Rules

- Every endpoint is a `path-action` chain in a `.xconf` file, not a hand-written servlet — e.g.
  `JsonAdminModule.preprocess` then `Admin.login` for an authenticated admin call. Reuse existing
  `path-action`s from `system`/`finder` modules rather than adding new Java unless the operation is
  genuinely new.
- Give the endpoint a `<permission name="view">` block; use `<blank/>` only for endpoints that are
  intentionally open.
- Keep the response shape documented: add/update a matching entry in
  `plugins/catalog/html/data/lists/endpoint/*.xml` with a `samplerequest`.
- `html/mcp.old` is legacy — new MCP-style work should go through `html/ai/mcp` instead.
- In `html/ai/<provider>/calls/*.json`: wrap every message `"content"` value in `#jesc(...)`
  (defined in `plugins/system/html/display/velocitymacros.vm`) — never hand-quote it. `#jesc(...)`'s
  argument is parsed by Velocity as a double-quoted string literal, so it may contain **zero**
  literal `"` characters (confirmed against the real engine: even a backslash-escaped `\"` fails to
  parse) — use single quotes for any inline JSON-shaped example text instead. Never hand-type a
  large reference blob (e.g. the full `aiskill` catalog) directly into one of these files; put it on
  the `AgentContext` as a variable and reference it as `$var` inside the `#jesc(...)` block instead
  — see the `create-java-ai-skill` skill for the full rule and a worked example.

### Validation Checklist

1. Clear the page cache (or restart) after adding/removing a `.xconf`/`.json` service file.
2. Call the endpoint directly (curl or the sample in `html/samples`) and confirm the JSON response
   matches the documented shape.
3. Confirm the endpoint enforces the intended permission (unauthenticated call should fail if it's
   not meant to be public).
4. If the endpoint is new, add it to `wadl.xml`/the endpoint docs so API consumers can discover it.

### Notes For Agents

- See `plugins/mediadb/html/services/authentication/README.txt` for a worked end-to-end example
  (request-code -> redeem-code login flow) that mirrors the conventions here.
- This plugin is the integration surface for external callers (mobile apps, third-party
  integrations, AI agents) — treat any change here as a compatibility-sensitive API change.
