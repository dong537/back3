#!/usr/bin/env node

import axios from 'axios';
import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
  Tool,
} from '@modelcontextprotocol/sdk/types.js';

const BACKEND_URL = (process.env.YIJING_BACKEND_URL || 'http://localhost:8088').replace(/\/+$/, '');

const TOOLS: Tool[] = [
  {
    name: 'yijing_generate_hexagram',
    description:
      'Generate an I Ching hexagram from a question using time, random, number, coin, or plum blossom methods.',
    inputSchema: {
      type: 'object',
      properties: {
        question: {
          type: 'string',
          description: 'The user question or intent for divination.',
        },
        method: {
          type: 'string',
          enum: ['time', 'random', 'number', 'coin', 'plum_blossom'],
          description: 'Hexagram generation method.',
          default: 'time',
        },
        seed: {
          type: 'string',
          description: 'Optional seed used when method is number.',
        },
      },
      required: ['question'],
    },
  },
  {
    name: 'yijing_interpret_hexagram',
    description:
      'Interpret an I Ching hexagram with optional changing lines for a specific question.',
    inputSchema: {
      type: 'object',
      properties: {
        hexagramId: {
          type: 'number',
          description: 'Hexagram id from 1 to 64.',
          minimum: 1,
          maximum: 64,
        },
        changingLines: {
          type: 'array',
          description: 'Optional changing line positions from 1 to 6.',
          items: {
            type: 'number',
            minimum: 1,
            maximum: 6,
          },
        },
        question: {
          type: 'string',
          description: 'The user question to interpret against the hexagram.',
        },
      },
      required: ['hexagramId', 'question'],
    },
  },
  {
    name: 'yijing_get_hexagram',
    description: 'Fetch detailed information for a specific I Ching hexagram.',
    inputSchema: {
      type: 'object',
      properties: {
        id: {
          type: 'number',
          description: 'Hexagram id from 1 to 64.',
          minimum: 1,
          maximum: 64,
        },
      },
      required: ['id'],
    },
  },
  {
    name: 'yijing_list_hexagrams',
    description: 'List all 64 I Ching hexagrams with their basic metadata.',
    inputSchema: {
      type: 'object',
      properties: {},
    },
  },
  {
    name: 'yijing_quick_divination',
    description:
      'Run a one-step I Ching divination flow that generates the hexagram and returns the direct result.',
    inputSchema: {
      type: 'object',
      properties: {
        question: {
          type: 'string',
          description: 'The user question for divination.',
        },
        method: {
          type: 'string',
          enum: ['time', 'random', 'coin', 'number', 'plum_blossom'],
          description: 'Hexagram generation method.',
          default: 'time',
        },
        seed: {
          type: 'string',
          description: 'Optional seed when using the number method.',
        },
      },
      required: ['question'],
    },
  },
  {
    name: 'tarot_get_spreads',
    description: 'List supported tarot spreads from the project backend.',
    inputSchema: {
      type: 'object',
      properties: {},
    },
  },
  {
    name: 'tarot_draw_cards',
    description:
      'Draw tarot cards for a question. Supports different spread types such as SINGLE and other backend-defined spreads.',
    inputSchema: {
      type: 'object',
      properties: {
        question: {
          type: 'string',
          description: 'Question or theme for the tarot reading.',
        },
        spreadType: {
          type: 'string',
          description: 'Tarot spread type. Defaults to SINGLE.',
          default: 'SINGLE',
        },
      },
      required: ['question'],
    },
  },
  {
    name: 'tarot_quick_draw',
    description: 'Run a quick single-card tarot reading for a question.',
    inputSchema: {
      type: 'object',
      properties: {
        question: {
          type: 'string',
          description: 'Question or theme for the tarot reading.',
        },
      },
      required: ['question'],
    },
  },
  {
    name: 'tarot_get_card_detail',
    description: 'Fetch detailed information for a tarot card by card id.',
    inputSchema: {
      type: 'object',
      properties: {
        cardId: {
          type: 'number',
          description: 'Tarot card id used by the backend.',
        },
      },
      required: ['cardId'],
    },
  },
  {
    name: 'tarot_list_cards',
    description: 'List all tarot cards with their basic metadata.',
    inputSchema: {
      type: 'object',
      properties: {},
    },
  },
  {
    name: 'tarot_get_daily_fortune',
    description: 'Get the daily fortune summary for a tarot card id.',
    inputSchema: {
      type: 'object',
      properties: {
        cardId: {
          type: 'number',
          description: 'Tarot card id used by the backend.',
        },
      },
      required: ['cardId'],
    },
  },
];

const server = new Server(
  {
    name: 'yijing-tarot-mcp',
    version: '1.1.0',
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

function toTextResult(payload: unknown) {
  return {
    content: [
      {
        type: 'text' as const,
        text: JSON.stringify(payload, null, 2),
      },
    ],
  };
}

function toErrorResult(error: unknown) {
  const axiosError = error as {
    response?: { data?: { message?: string } | string };
    message?: string;
  };
  const responseData = axiosError.response?.data;
  const fallback =
    typeof responseData === 'string'
      ? responseData
      : responseData && typeof responseData === 'object' && 'message' in responseData
        ? String(responseData.message)
        : axiosError.message || 'Unknown MCP error';

  return {
    content: [
      {
        type: 'text' as const,
        text: `Error: ${fallback}`,
      },
    ],
    isError: true,
  };
}

async function callTool(name: string, args: Record<string, unknown>) {
  switch (name) {
    case 'yijing_generate_hexagram':
      return toTextResult(
        (
          await axios.post(`${BACKEND_URL}/api/yijing/hexagram/generate`, {
            question: args.question,
            method: args.method || 'time',
            seed: args.seed,
          })
        ).data
      );

    case 'yijing_interpret_hexagram':
      return toTextResult(
        (
          await axios.post(`${BACKEND_URL}/api/yijing/hexagram/interpret`, {
            hexagramId: args.hexagramId,
            changingLines: args.changingLines || [],
            question: args.question,
          })
        ).data
      );

    case 'yijing_get_hexagram':
      return toTextResult(
        (await axios.get(`${BACKEND_URL}/api/yijing/hexagram/${args.id}`)).data
      );

    case 'yijing_list_hexagrams':
      return toTextResult((await axios.get(`${BACKEND_URL}/api/yijing/hexagrams`)).data);

    case 'yijing_quick_divination':
      return toTextResult(
        (
          await axios.post(`${BACKEND_URL}/api/standalone/yijing/quick-divination`, {
            question: args.question,
            method: args.method || 'time',
            seed: args.seed || '',
          })
        ).data
      );

    case 'tarot_get_spreads':
      return toTextResult((await axios.get(`${BACKEND_URL}/api/tarot/spreads`)).data);

    case 'tarot_draw_cards':
      return toTextResult(
        (
          await axios.post(`${BACKEND_URL}/api/tarot/draw`, {
            question: args.question || '',
            spreadType: args.spreadType || 'SINGLE',
          })
        ).data
      );

    case 'tarot_quick_draw':
      return toTextResult(
        (
          await axios.post(`${BACKEND_URL}/api/tarot/quick-draw`, {
            question: args.question || '',
          })
        ).data
      );

    case 'tarot_get_card_detail':
      return toTextResult(
        (await axios.get(`${BACKEND_URL}/api/tarot/card/${args.cardId}`)).data
      );

    case 'tarot_list_cards':
      return toTextResult((await axios.get(`${BACKEND_URL}/api/tarot/cards`)).data);

    case 'tarot_get_daily_fortune':
      return toTextResult(
        (await axios.get(`${BACKEND_URL}/api/tarot/daily-fortune/${args.cardId}`)).data
      );

    default:
      throw new Error(`Unknown tool: ${name}`);
  }
}

server.setRequestHandler(ListToolsRequestSchema, async () => ({
  tools: TOOLS,
}));

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    return await callTool(name, (args || {}) as Record<string, unknown>);
  } catch (error) {
    return toErrorResult(error);
  }
});

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);

  console.error('Yijing and tarot MCP server started.');
  console.error(`Backend URL: ${BACKEND_URL}`);
}

main().catch((error) => {
  console.error('Failed to start MCP server:', error);
  process.exit(1);
});
