# 项目结构文档

## 📁 项目概览

```
baziback/
├── 📂 后端 (Spring Boot)
│   ├── src/main/java/com/example/demo/
│   ├── src/main/resources/
│   └── pom.xml
├── 📂 前端 (React + Vite)
│   ├── src-frontend/
│   ├── vite.config.js
│   └── package.json
├── 📂 数据库脚本
│   └── database/
├── 📂 移动端 (Capacitor)
│   ├── android/
│   └── ios/
├── 📂 部署相关
│   └── deploy/
├── 📂 MCP 服务
│   ├── mcp-server/
│   └── mcp-src/
└── 📂 文档
    └── *.md
```

---

## 📂 详细目录结构

### 1. 后端代码 (`src/main/java/com/example/demo/`)

```
com/example/demo/
├── 📂 bazi/                    # 八字排盘模块
│   ├── analyzer/              # 八字分析器
│   ├── constants/             # 常量定义（天干、地支、十神等）
│   ├── controller/            # 控制器
│   ├── dto/                   # 数据传输对象
│   ├── model/                 # 数据模型
│   ├── service/               # 业务逻辑
│   └── util/                  # 工具类
│
├── 📂 yijing/                 # 易经占卜模块
│   ├── model/                 # 卦象模型
│   ├── repository/            # 数据仓库（64卦数据）
│   └── service/               # 业务逻辑
│
├── 📂 tarot/                  # 塔罗牌模块
│   ├── controller/            # 控制器
│   ├── model/                 # 塔罗牌模型
│   ├── repository/            # 数据仓库（78张牌）
│   └── service/               # 业务逻辑
│
├── 📂 client/                 # 外部客户端
│   ├── McpSseClient.java      # MCP SSE 客户端
│   ├── McpStarClient.java     # 星座 MCP 客户端
│   └── McpZiweiClient.java    # 紫微 MCP 客户端
│
├── 📂 common/                 # 通用类
│   └── Result.java            # 统一响应结果
│
├── 📂 config/                 # 配置类
│   ├── CacheConfig.java       # 缓存配置
│   ├── CorsConfig.java        # 跨域配置
│   ├── RateLimitConfig.java   # 限流配置
│   ├── RetryConfig.java       # 重试配置
│   ├── SecurityConfig.java    # 安全配置
│   ├── mcp/                   # MCP 配置
│   └── security/              # 安全相关配置
│
├── 📂 controller/             # REST API 控制器
│   ├── BaZiController.java     # 八字接口
│   ├── DeepSeekController.java # DeepSeek AI 接口
│   ├── ReasoningController.java # 推理接口
│   ├── StandaloneYijingController.java # 独立易经接口
│   ├── StarController.java     # 星座接口
│   ├── UserController.java     # 用户接口
│   ├── YijingController.java   # 易经接口
│   └── ZiweiController.java   # 紫微接口
│
├── 📂 dto/                     # 数据传输对象
│   ├── request/               # 请求 DTO（35个文件）
│   └── response/              # 响应 DTO（21个文件）
│
├── 📂 entity/                  # 数据库实体类
│   ├── User.java              # 用户实体
│   ├── UserReferral.java      # 用户推荐关系
│   ├── DailyCheckin.java      # 每日签到
│   ├── Task.java              # 任务定义
│   ├── UserTaskProgress.java  # 用户任务进度
│   ├── Achievement.java      # 成就定义
│   ├── UserAchievement.java  # 用户成就记录
│   ├── InviteRecord.java     # 邀请记录
│   └── ShareRecord.java      # 分享记录
│
├── 📂 exception/               # 异常处理
│   ├── BusinessException.java # 业务异常
│   ├── GlobalExceptionHandler.java # 全局异常处理
│   └── McpApiException.java   # MCP API 异常
│
├── 📂 mapper/                  # MyBatis Mapper
│   └── UserMapper.java        # 用户 Mapper
│
├── 📂 service/                 # 业务服务层
│   ├── DeepSeekService.java   # DeepSeek AI 服务
│   ├── ReasoningService.java  # 推理服务
│   ├── UserService.java       # 用户服务
│   ├── YijingService.java     # 易经服务
│   ├── YijingReasoningService.java # 易经推理服务
│   ├── ZiweiService.java      # 紫微服务
│   └── ZodiacService.java     # 星座服务
│
├── 📂 util/                    # 工具类
│   └── JwtUtil.java           # JWT 工具
│
└── Demo1Application.java      # Spring Boot 启动类
```

---

### 2. 前端代码 (`src-frontend/`)

```
src-frontend/
├── 📂 api/                     # API 接口
│   └── index.js               # API 统一导出
│
├── 📂 components/              # React 组件
│   ├── AchievementBadge.jsx   # 成就徽章
│   ├── Button.jsx             # 按钮组件
│   ├── Card.jsx               # 卡片组件
│   ├── DailyCheckin.jsx      # 每日签到
│   ├── ErrorBoundary.jsx     # 错误边界
│   ├── FloatingActionButton.jsx # 浮动操作按钮
│   ├── HexagramAnimation.jsx  # 卦象动画
│   ├── HistoryModal.jsx      # 历史记录弹窗
│   ├── Input.jsx             # 输入组件
│   ├── Layout.jsx            # 布局组件
│   ├── PaymentModal.jsx      # 支付弹窗
│   ├── ShareModal.jsx        # 分享弹窗
│   ├── SharePrompt.jsx       # 分享引导
│   ├── SimpleChart.jsx       # 简单图表
│   ├── SkeletonLoader.jsx    # 骨架屏
│   ├── StatsCard.jsx         # 统计卡片
│   ├── TaskList.jsx          # 任务列表
│   ├── ThinkingChain.jsx     # 思考链组件
│   └── Toast.jsx              # Toast 通知
│
├── 📂 config/                  # 配置文件
│   └── themes.js              # 主题配置
│
├── 📂 context/                 # React Context
│   ├── AuthContext.jsx        # 认证上下文
│   └── ThemeContext.jsx       # 主题上下文
│
├── 📂 i18n/                    # 国际化
│   ├── index.js               # i18n 配置
│   ├── zh-CN.json             # 中文翻译
│   └── en-US.json             # 英文翻译
│
├── 📂 pages/                   # 页面组件
│   ├── Home.jsx               # 首页
│   ├── YijingPage.jsx         # 易经占卜页
│   ├── TarotPage.jsx          # 塔罗牌页
│   ├── ZodiacPage.jsx         # 星座页
│   ├── BaziPage.jsx           # 八字排盘页
│   ├── AIPage.jsx             # AI 页面
│   ├── DashboardPage.jsx      # 数据看板
│   ├── ReferralPage.jsx       # 邀请好友页
│   └── LoginPage.jsx          # 登录页
│
├── 📂 utils/                   # 工具函数
│   ├── referral.js            # 推荐系统工具
│   ├── referralHelper.js      # 推荐辅助函数
│   ├── renderers.jsx          # 渲染工具
│   ├── storage.js             # 本地存储工具
│   └── textSanitizer.js       # 文本清理工具
│
├── App.jsx                     # 根组件
├── main.jsx                    # 入口文件
└── index.css                   # 全局样式
```

---

### 3. 数据库脚本 (`database/`)

```
database/
├── init_user_table.sql              # 用户表初始化
├── add_credit_system_tables.sql    # 积分系统表
├── add_referral_system_tables.sql  # 推荐系统表
└── cleanup_and_init.sql            # 清理和初始化脚本
```

---

### 4. 移动端 (`android/`, `ios/`)

```
android/
├── app/                        # Android 应用代码
├── build.gradle                # Gradle 构建配置
├── gradle/                     # Gradle 包装器
└── local.properties            # 本地配置

ios/
├── App/                        # iOS 应用代码
└── capacitor-cordova-ios-plugins/ # iOS 插件
```

---

### 5. 部署相关 (`deploy/`)

```
deploy/
├── README.md                   # 部署说明
├── DEPLOY_COMMANDS.md          # 部署命令
├── MANUAL_PACKAGE_GUIDE.md     # 手动打包指南
├── POST_DEPLOYMENT_GUIDE.md    # 部署后指南
├── WINDOWS_PACKAGE_GUIDE.md    # Windows 打包指南
├── deploy-to-server.sh         # 服务器部署脚本
├── package-for-server.ps1      # PowerShell 打包脚本
└── package.bat                 # Windows 批处理打包
```

---

### 6. MCP 服务 (`mcp-server/`, `mcp-src/`)

```
mcp-server/
├── src/                        # TypeScript 源码
├── dist/                       # 编译输出
├── package.json                # 依赖配置
└── README.md                   # MCP 服务说明

mcp-src/
└── index.ts                    # MCP 入口文件
```

---

### 7. 配置文件（根目录）

```
根目录/
├── pom.xml                     # Maven 配置
├── package.json                # Node.js 配置
├── vite.config.js             # Vite 配置
├── tailwind.config.js         # Tailwind CSS 配置
├── postcss.config.js          # PostCSS 配置
├── capacitor.config.ts        # Capacitor 配置
├── tsconfig.mcp.json          # TypeScript MCP 配置
└── application.yml            # Spring Boot 配置（src/main/resources/）
```

---

### 8. 文档文件（根目录）

```
根目录/
├── README.md                   # 项目主文档
├── DATABASE_SCHEMA.md         # 数据库结构文档
├── PRODUCT_IMPROVEMENTS.md    # 产品改进文档
├── REFERRAL_STRATEGY.md       # 推荐策略文档
├── PROJECT_STRUCTURE.md       # 项目结构文档（本文件）
└── USER_GROWTH_STRATEGY.md   # 用户增长策略文档
```

---

## 🎯 模块说明

### 后端模块

| 模块 | 说明 | 主要功能 |
|------|------|---------|
| `bazi` | 八字排盘 | 八字计算、分析、AI解读 |
| `yijing` | 易经占卜 | 64卦、起卦、解读 |
| `tarot` | 塔罗牌 | 78张牌、12种牌阵 |
| `client` | 外部客户端 | MCP 协议客户端 |
| `config` | 配置 | 系统配置、安全配置 |
| `controller` | 控制器 | REST API 接口 |
| `service` | 服务层 | 业务逻辑实现 |
| `entity` | 实体类 | 数据库实体映射 |

### 前端模块

| 模块 | 说明 | 主要功能 |
|------|------|---------|
| `pages` | 页面 | 各功能页面 |
| `components` | 组件 | 可复用组件 |
| `api` | API | 接口调用封装 |
| `utils` | 工具 | 工具函数 |
| `context` | 上下文 | 全局状态管理 |
| `i18n` | 国际化 | 多语言支持 |

---

## 📋 命名规范

### Java 后端
- **类名**：大驼峰（PascalCase），如 `UserService.java`
- **包名**：小写，如 `com.example.demo.service`
- **常量**：全大写下划线，如 `MAX_RETRY_COUNT`
- **方法名**：小驼峰（camelCase），如 `getUserById()`

### JavaScript/React 前端
- **组件文件**：大驼峰，如 `UserProfile.jsx`
- **工具文件**：小驼峰，如 `formatDate.js`
- **常量文件**：小写下划线，如 `api_config.js`
- **组件名**：大驼峰，如 `const UserProfile = () => {}`

### 数据库
- **表名**：小写下划线，前缀 `tb_`，如 `tb_user`
- **字段名**：小写下划线，如 `user_id`
- **索引名**：小写下划线，前缀 `idx_`，如 `idx_user_id`

---

## 🔄 数据流

### 请求流程
```
前端 (React)
  ↓ HTTP Request
后端 Controller
  ↓ 参数验证
Service 层
  ↓ 业务逻辑
Mapper/Repository
  ↓ SQL 查询
数据库 (MySQL)
  ↓ 返回数据
Service 层
  ↓ 数据处理
Controller
  ↓ JSON Response
前端 (React)
```

### 状态管理
```
前端状态管理：
- AuthContext: 用户认证状态
- ThemeContext: 主题状态
- localStorage: 本地数据存储（历史、收藏、积分等）
```

---

## 🛠️ 开发规范

### 代码组织
1. **按功能模块划分**：每个功能模块独立目录
2. **分层清晰**：Controller → Service → Mapper → Entity
3. **职责单一**：每个类只负责一个功能
4. **依赖注入**：使用 Spring 的依赖注入

### 文件命名
- **后端**：`功能名+类型.java`，如 `UserService.java`
- **前端**：`功能名+类型.jsx`，如 `UserProfile.jsx`
- **配置文件**：`功能名.config.js`，如 `vite.config.js`

### 注释规范
- **类注释**：说明类的用途
- **方法注释**：说明参数、返回值、异常
- **复杂逻辑**：添加行内注释

---

## 📦 构建输出

### 后端构建
```
target/
├── bazi-0.0.1-SNAPSHOT.jar    # 可执行 JAR
└── classes/                    # 编译后的类文件
```

### 前端构建
```
dist/
├── index.html                  # 入口 HTML
└── assets/                     # 静态资源
    ├── *.js                    # JavaScript 文件
    └── *.css                   # CSS 文件
```

---

## 🔍 查找文件指南

### 查找后端代码
- **Controller**: `src/main/java/com/example/demo/controller/`
- **Service**: `src/main/java/com/example/demo/service/`
- **Entity**: `src/main/java/com/example/demo/entity/`
- **配置**: `src/main/resources/application.yml`

### 查找前端代码
- **页面**: `src-frontend/pages/`
- **组件**: `src-frontend/components/`
- **API**: `src-frontend/api/`
- **工具**: `src-frontend/utils/`

### 查找数据库脚本
- **所有 SQL**: `database/`
- **表结构文档**: `DATABASE_SCHEMA.md`

---

## 📚 相关文档

- [README.md](./README.md) - 项目主文档
- [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - 数据库结构
- [REFERRAL_STRATEGY.md](./REFERRAL_STRATEGY.md) - 推荐策略
- [PRODUCT_IMPROVEMENTS.md](./PRODUCT_IMPROVEMENTS.md) - 产品改进

---

**最后更新**：2025-01-08  
**版本**：1.0.0  
**维护者**：开发团队
