# Electron 配置说明

## 📁 文件结构

```
electron/
├── main.js          # Electron 主进程文件
├── icon.ico         # Windows 图标（需要准备）
├── icon.icns        # Mac 图标（需要准备）
├── icon.png         # Linux 图标（需要准备）
└── README.md        # 本文件
```

## 🎨 准备应用图标

### Windows 图标 (.ico)

1. 准备一张 256x256 或更大的 PNG 图片
2. 使用在线工具转换为 ICO 格式：
   - https://convertio.co/zh/png-ico/
   - https://www.icoconverter.com/
3. 保存为 `electron/icon.ico`

### Mac 图标 (.icns)

1. 准备一张 512x512 或更大的 PNG 图片
2. 使用在线工具转换为 ICNS 格式：
   - https://cloudconvert.com/png-to-icns
   - https://iconverticons.com/online/
3. 保存为 `electron/icon.icns`

### Linux 图标 (.png)

1. 准备一张 512x512 的 PNG 图片
2. 保存为 `electron/icon.png`

## 🔧 配置说明

### main.js

这是 Electron 应用的主进程文件，负责：

- 创建应用窗口
- 加载前端应用
- 管理应用生命周期
- 可选：启动后端服务

### 自定义配置

编辑 `main.js` 可以修改：

```javascript
// 窗口大小
width: 1200,
height: 800,

// 最小尺寸
minWidth: 800,
minHeight: 600,

// 是否显示菜单栏
autoHideMenuBar: false,

// 其他 Electron 选项
```

## 🚀 开发模式

运行开发模式（连接到 Vite 开发服务器）：

```powershell
npm run electron:dev
```

## 📦 打包

打包成桌面应用：

```powershell
# Windows
npm run electron:build:win

# Mac
npm run electron:build:mac

# Linux
npm run electron:build:linux
```

## 📝 注意事项

1. **后端服务**: 默认情况下，应用会连接到 `http://localhost:8088`
   - 如果需要内置后端，取消 `main.js` 中 `startBackend()` 的注释
   - 确保后端 JAR 文件在正确位置

2. **生产环境**: 打包时确保已运行 `npm run build` 生成 `dist/` 目录

3. **图标文件**: 如果没有图标文件，应用会使用默认图标

4. **安全**: 生产环境建议启用 `contextIsolation` 和禁用 `nodeIntegration`
