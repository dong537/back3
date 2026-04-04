# Yijing Tarot OpenClaw Plugin

This plugin adapts the local `baziback` project to OpenClaw using a Codex-compatible bundle.

## What is included

- `.codex-plugin/plugin.json` for OpenClaw discovery
- `.mcp.json` for MCP registration
- `scripts/start-stdio-mcp.mjs` to build and start the stdio MCP entry automatically
- `skills/yijing-tarot-assistant/SKILL.md` to guide tool usage

## Runtime requirements

- Node.js 18+
- The `baziback` backend running and reachable through `YIJING_BACKEND_URL`
- Repository dependencies installed with `npm install`

## Notes

- By default the plugin expects the backend at `http://localhost:8088`.
- If your repo is not in the original relative location, set `YIJING_REPO_ROOT` in the OpenClaw MCP environment.
- The first launch can trigger `npm run mcp:build` automatically if `mcp-dist/index.js` does not exist yet.
