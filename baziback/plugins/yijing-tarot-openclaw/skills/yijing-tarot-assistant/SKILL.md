---
name: yijing-tarot-assistant
description: Use this skill when the user wants I Ching divination, tarot readings, hexagram lookup, or tarot card lookup through the baziback tools exposed to OpenClaw.
---

# Yijing Tarot Assistant

Use the bundled MCP tools before answering from memory.

## What this plugin can do

- Generate I Ching hexagrams for a question.
- Interpret a specific hexagram with optional changing lines.
- List or fetch hexagram reference data.
- Run quick tarot draws and larger tarot spreads.
- Fetch tarot card details and daily fortune summaries.

## Working style

- Treat these readings as cultural and entertainment-oriented guidance, not medical, legal, or financial advice.
- If the user asks for a reading, call the relevant tool first and ground the answer in the tool result.
- If the user only wants background knowledge, use lookup tools before summarizing.
- Prefer `yijing_quick_divination` for general I Ching readings unless the user explicitly asks for a step-by-step generate plus interpret flow.
- Prefer `tarot_quick_draw` for a single-card reading and `tarot_draw_cards` when the user asks for a specific spread.

## Tool map

- `yijing_generate_hexagram`: create a hexagram.
- `yijing_interpret_hexagram`: interpret a known hexagram id.
- `yijing_get_hexagram`: fetch one hexagram reference entry.
- `yijing_list_hexagrams`: browse all hexagrams.
- `yijing_quick_divination`: fastest end-to-end I Ching flow.
- `tarot_get_spreads`: discover supported tarot spreads.
- `tarot_draw_cards`: run a multi-card or named tarot spread.
- `tarot_quick_draw`: single-card tarot reading.
- `tarot_get_card_detail`: inspect a tarot card.
- `tarot_list_cards`: browse the tarot deck.
- `tarot_get_daily_fortune`: get the daily summary for one tarot card.
