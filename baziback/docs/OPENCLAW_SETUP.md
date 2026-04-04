# OpenClaw Setup

This project now includes a repo-local OpenClaw bundle at `plugins/yijing-tarot-openclaw`.

## Included adaptation

- OpenClaw bundle manifest: `plugins/yijing-tarot-openclaw/.codex-plugin/plugin.json`
- MCP bundle config: `plugins/yijing-tarot-openclaw/.mcp.json`
- Auto-launch script for the stdio MCP server: `plugins/yijing-tarot-openclaw/scripts/start-stdio-mcp.mjs`
- Plugin marketplace entry: `.agents/plugins/marketplace.json`

## What OpenClaw uses

OpenClaw launches the local bundle, which starts the repo's stdio MCP server from `mcp-dist/index.js`.

If `mcp-dist/index.js` is missing, the launcher runs:

```bash
npm run mcp:build
```

## Before installing in OpenClaw

1. Install Node dependencies in the repo root.
2. Start the backend service on the host you want OpenClaw to call.
3. Make sure `YIJING_BACKEND_URL` points to that backend.

Default backend URL:

```text
http://localhost:8088
```

## Optional environment overrides

You can override these values in the OpenClaw MCP environment:

```json
{
  "YIJING_BACKEND_URL": "http://localhost:8088",
  "YIJING_REPO_ROOT": "C:/Users/Lenovo/Desktop/n8n/back3/baziback"
}
```

## Tool coverage

The OpenClaw bundle exposes these project tools through MCP:

- `yijing_generate_hexagram`
- `yijing_interpret_hexagram`
- `yijing_get_hexagram`
- `yijing_list_hexagrams`
- `yijing_quick_divination`
- `tarot_get_spreads`
- `tarot_draw_cards`
- `tarot_quick_draw`
- `tarot_get_card_detail`
- `tarot_list_cards`
- `tarot_get_daily_fortune`

## Verification

After the plugin is installed, test with prompts such as:

- `Run a quick I Ching divination for my question about work this week.`
- `Draw a tarot spread for my current relationship question.`
- `Look up hexagram 1 and summarize it.`
