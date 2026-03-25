#!/usr/bin/env node

import express, { Request, Response } from 'express';
import cors from 'cors';
import axios from 'axios';
import { randomUUID } from 'crypto';

// 配置
const PORT = parseInt(process.env.MCP_PORT || '3001', 10);
const BACKEND_URL = process.env.YIJING_BACKEND_URL || 'http://localhost:8088';

// Express 应用
const app = express();
app.use(cors());
app.use(express.json());

// MCP 工具类型定义
interface Tool {
  name: string;
  description: string;
  inputSchema: object;
}

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

// 存储 SSE 连接的 session
const sessions = new Map<string, Response>();

// 调用工具的核心逻辑
async function callTool(name: string, args: any): Promise<any> {
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
          content: [{ type: 'text', text: JSON.stringify(response.data, null, 2) }],
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
          content: [{ type: 'text', text: JSON.stringify(response.data, null, 2) }],
        };
      }

      case 'yijing_get_hexagram': {
        const response = await axios.get(
          `${BACKEND_URL}/api/yijing/hexagram/${params.id}`
        );
        return {
          content: [{ type: 'text', text: JSON.stringify(response.data, null, 2) }],
        };
      }

      case 'yijing_list_hexagrams': {
        const response = await axios.get(`${BACKEND_URL}/api/yijing/hexagrams`);
        return {
          content: [{ type: 'text', text: JSON.stringify(response.data, null, 2) }],
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
          content: [{ type: 'text', text: JSON.stringify(response.data, null, 2) }],
        };
      }

      default:
        throw new Error(`Unknown tool: ${name}`);
    }
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || error.message;
    return {
      content: [{ type: 'text', text: `错误: ${errorMessage}` }],
      isError: true,
    };
  }
}

// 发送 SSE 消息
function sendSSE(res: Response, data: any) {
  res.write(`data: ${JSON.stringify(data)}\n\n`);
}

// 存储心跳定时器
const heartbeats = new Map<string, NodeJS.Timeout>();

// SSE 端点 - 建立连接并返回 sessionId
app.get('/sse', (req: Request, res: Response) => {
  const sessionId = randomUUID();
  
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('X-Accel-Buffering', 'no');
  
  // 立即刷新响应头
  res.flushHeaders();
  
  // 发送 endpoint 信息
  sendSSE(res, { 
    jsonrpc: '2.0',
    method: 'endpoint',
    params: { 
      endpoint: `/message?sessionId=${sessionId}` 
    }
  });
  
  // 设置心跳，每 15 秒发送一次保持连接
  const heartbeat = setInterval(() => {
    res.write(': heartbeat\n\n');
  }, 15000);
  
  sessions.set(sessionId, res);
  heartbeats.set(sessionId, heartbeat);
  console.log(`SSE 连接建立: ${sessionId}`);
  
  req.on('close', () => {
    const hb = heartbeats.get(sessionId);
    if (hb) clearInterval(hb);
    heartbeats.delete(sessionId);
    sessions.delete(sessionId);
    console.log(`SSE 连接关闭: ${sessionId}`);
  });
});

// 消息端点 - 接收 JSON-RPC 请求
app.post('/message', async (req: Request, res: Response) => {
  const sessionId = req.query.sessionId as string;
  const sseRes = sessions.get(sessionId);
  
  const { jsonrpc, id, method, params } = req.body;
  
  try {
    let result: any;
    
    if (method === 'initialize') {
      result = {
        protocolVersion: '2024-11-05',
        capabilities: { tools: {} },
        serverInfo: { name: 'yijing-tarot-mcp', version: '1.0.0' },
      };
    } else if (method === 'tools/list') {
      result = { tools: TOOLS };
    } else if (method === 'tools/call') {
      result = await callTool(params.name, params.arguments);
    } else {
      throw new Error(`Unknown method: ${method}`);
    }
    
    const response = { jsonrpc: '2.0', id, result };
    
    // 如果有 SSE 连接，通过 SSE 发送
    if (sseRes) {
      sendSSE(sseRes, response);
    }
    
    res.json(response);
  } catch (error: any) {
    const errorResponse = {
      jsonrpc: '2.0',
      id,
      error: { code: -32603, message: error.message },
    };
    
    if (sseRes) {
      sendSSE(sseRes, errorResponse);
    }
    
    res.json(errorResponse);
  }
});

// 兼容模式：POST /mcp 直接返回 SSE 响应（ModelScope 风格）
app.post('/mcp', async (req: Request, res: Response) => {
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.setHeader('Access-Control-Allow-Origin', '*');
  
  const { jsonrpc, id, method, params } = req.body;
  
  try {
    let result: any;
    
    if (method === 'initialize') {
      result = {
        protocolVersion: '2024-11-05',
        capabilities: { tools: {} },
        serverInfo: { name: 'yijing-tarot-mcp', version: '1.0.0' },
      };
    } else if (method === 'tools/list') {
      result = { tools: TOOLS };
    } else if (method === 'tools/call') {
      result = await callTool(params.name, params.arguments);
    } else {
      throw new Error(`Unknown method: ${method}`);
    }
    
    sendSSE(res, { jsonrpc: '2.0', id, result });
    res.end();
  } catch (error: any) {
    sendSSE(res, {
      jsonrpc: '2.0',
      id,
      error: { code: -32603, message: error.message },
    });
    res.end();
  }
});

// Streamable HTTP 端点 - 纯 JSON 响应（不是 SSE）
app.post('/http', async (req: Request, res: Response) => {
  res.setHeader('Content-Type', 'application/json');
  res.setHeader('Access-Control-Allow-Origin', '*');
  
  const { jsonrpc, id, method, params } = req.body;
  
  try {
    let result: any;
    
    if (method === 'initialize') {
      result = {
        protocolVersion: '2024-11-05',
        capabilities: { tools: {} },
        serverInfo: { name: 'yijing-tarot-mcp', version: '1.0.0' },
      };
    } else if (method === 'tools/list') {
      result = { tools: TOOLS };
    } else if (method === 'tools/call') {
      result = await callTool(params.name, params.arguments);
    } else {
      throw new Error(`Unknown method: ${method}`);
    }
    
    res.json({ jsonrpc: '2.0', id, result });
  } catch (error: any) {
    res.json({
      jsonrpc: '2.0',
      id,
      error: { code: -32603, message: error.message },
    });
  }
});

// 健康检查
app.get('/health', (req: Request, res: Response) => {
  res.json({ status: 'ok', backend: BACKEND_URL });
});

// 启动服务器
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 易经塔罗 MCP 服务已启动`);
  console.log(`   端口: ${PORT}`);
  console.log(`   后端: ${BACKEND_URL}`);
  console.log(`   HTTP 端点: http://0.0.0.0:${PORT}/http`);
  console.log(`   SSE 端点: http://0.0.0.0:${PORT}/sse`);
  console.log(`   MCP 端点: http://0.0.0.0:${PORT}/mcp`);
});
