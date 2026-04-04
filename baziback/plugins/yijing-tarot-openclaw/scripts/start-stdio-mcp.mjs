#!/usr/bin/env node

import { spawnSync } from 'child_process';
import { existsSync } from 'fs';
import path from 'path';
import { fileURLToPath, pathToFileURL } from 'url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const pluginRoot = path.resolve(scriptDir, '..');
const repoRoot = (process.env.YIJING_REPO_ROOT || path.resolve(pluginRoot, '../..')).trim();
const mcpEntry = path.join(repoRoot, 'mcp-dist', 'index.js');

function ensureBuiltEntry() {
  if (existsSync(mcpEntry)) {
    return;
  }

  const npmCommand = process.platform === 'win32' ? 'npm.cmd' : 'npm';
  console.error(`[yijing-tarot-openclaw] Building stdio MCP entry in ${repoRoot}`);

  const build = spawnSync(npmCommand, ['run', 'mcp:build'], {
    cwd: repoRoot,
    env: process.env,
    stdio: 'inherit',
  });

  if (build.status !== 0) {
    process.exit(build.status ?? 1);
  }

  if (!existsSync(mcpEntry)) {
    console.error(`[yijing-tarot-openclaw] Missing built entry: ${mcpEntry}`);
    process.exit(1);
  }
}

ensureBuiltEntry();
await import(pathToFileURL(mcpEntry).href);
