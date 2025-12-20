#!/usr/bin/env node

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
  Tool,
} from '@modelcontextprotocol/sdk/types.js';
import axios from 'axios';

// 配置后端服务地址
const BACKEND_URL = process.env.YIJING_BACKEND_URL || 'http://localhost:8088';

// 定义 MCP 工具
const TOOLS: Tool[] = [
  {
    name: 'yijing_generate_hexagram',
    description: '生成易经卦象。支持多种起卦方法：时间起卦(time)、随机起卦(random)、数字起卦(number)、金钱卦(coin)、梅花易数(plum_blossom)',
    inputSchema: {
      type: 'object',
      properties: {
        question: {
          type: 'string',
          description: '占卜的问题或意图',
        },
        method: {
          type: 'string',
          enum: ['time', 'random', 'number', 'coin', 'plum_blossom'],
          description: '起卦方法',
          default: 'time',
        },
        seed: {
          type: 'string',
          description: '数字起卦时使用的种子（仅method为number时需要）',
        },
      },
      required: ['question', 'method'],
    },
  },
  {
    name: 'yijing_interpret_hexagram',
    description: '解读易经卦象。根据卦象ID、动爻和问题提供详细的解读',
    inputSchema: {
      type: 'object',
      properties: {
        hexagramId: {
          type: 'number',
          description: '卦象ID (1-64)',
          minimum: 1,
          maximum: 64,
        },
        changingLines: {
          type: 'array',
          items: {
            type: 'number',
            minimum: 1,
            maximum: 6,
          },
          description: '动爻位置数组 (1-6)',
        },
        question: {
          type: 'string',
          description: '占卜的问题',
        },
      },
      required: ['hexagramId', 'question'],
    },
  },
  {
    name: 'yijing_get_hexagram',
    description: '获取指定卦象的详细信息，包括卦辞、象辞、爻辞等',
    inputSchema: {
      type: 'object',
      properties: {
        id: {
          type: 'number',
          description: '卦象ID (1-64)',
          minimum: 1,
          maximum: 64,
        },
      },
      required: ['id'],
    },
  },
  {
    name: 'yijing_list_hexagrams',
    description: '获取所有64卦的列表，包含基本信息',
    inputSchema: {
      type: 'object',
      properties: {},
    },
  },
  {
    name: 'yijing_quick_divination',
    description: '快速占卜 - 一次性完成起卦和解读。这是最便捷的占卜方式',
    inputSchema: {
      type: 'object',
      properties: {
        question: {
          type: 'string',
          description: '占卜的问题',
        },
        method: {
          type: 'string',
          enum: ['time', 'random', 'coin'],
          description: '起卦方法',
          default: 'time',
        },
      },
      required: ['question'],
    },
  },
];

// 创建 MCP 服务器
const server = new Server(
  {
    name: 'yijing-tarot-mcp',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// 处理工具列表请求
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: TOOLS,
  };
});

// 处理工具调用请求
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;
  const params = args || {};

  try {
    switch (name) {
      case 'yijing_generate_hexagram': {
        const response = await axios.post(
          `${BACKEND_URL}/api/yijing/hexagram/generate`,
          {
            question: params.question,
            method: params.method || 'time',
            seed: params.seed,
          }
        );

        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      case 'yijing_interpret_hexagram': {
        const response = await axios.post(
          `${BACKEND_URL}/api/yijing/hexagram/interpret`,
          {
            hexagramId: params.hexagramId,
            changingLines: params.changingLines || [],
            question: params.question,
          }
        );

        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      case 'yijing_get_hexagram': {
        const response = await axios.get(
          `${BACKEND_URL}/api/yijing/hexagram/${params.id}`
        );

        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      case 'yijing_list_hexagrams': {
        const response = await axios.get(
          `${BACKEND_URL}/api/yijing/hexagrams`
        );

        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      case 'yijing_quick_divination': {
        const response = await axios.post(
          `${BACKEND_URL}/api/standalone/yijing/quick-divination`,
          {
            question: params.question,
            method: params.method || 'time',
          }
        );

        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(response.data, null, 2),
            },
          ],
        };
      }

      default:
        throw new Error(`Unknown tool: ${name}`);
    }
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || error.message;
    return {
      content: [
        {
          type: 'text',
          text: `错误: ${errorMessage}`,
        },
      ],
      isError: true,
    };
  }
});

// 启动服务器
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  
  console.error('易经塔罗 MCP 服务已启动');
  console.error(`后端地址: ${BACKEND_URL}`);
}

main().catch((error) => {
  console.error('服务启动失败:', error);
  process.exit(1);
});
