# 快速配置广告位ID

## 🚀 三步配置

### 1️⃣ 创建 .env 文件

在项目根目录 `back3/baziback/` 创建 `.env` 文件

**Windows 快速创建：**
```powershell
cd C:\Users\Lenovo\Desktop\n8n\back3\baziback
echo. > .env
```

### 2️⃣ 添加配置内容

打开 `.env` 文件，复制以下内容并替换广告位ID：

```env
# 穿山甲广告配置
VITE_PANGLE_SLOT_ID=你的广告位ID
VITE_PANGLE_SPLASH_SLOT_ID=你的开屏广告位ID
```

**示例：**
```env
VITE_PANGLE_SLOT_ID=1234567890
VITE_PANGLE_SPLASH_SLOT_ID=0987654321
```

### 3️⃣ 重启开发服务器

```bash
# 停止当前服务器（Ctrl+C）
npm run dev
```

---

## 📍 文件位置

```
back3/baziback/
├── .env          ← 在这里创建
├── package.json
└── ...
```

---

## 🔑 获取广告位ID

1. 登录 https://www.csjplatform.com/
2. 进入"应用管理" → "广告位管理"
3. 创建或查看广告位
4. 复制"广告位ID"

---

## ✅ 验证配置

打开浏览器控制台（F12），输入：
```javascript
console.log(import.meta.env.VITE_PANGLE_SLOT_ID)
```

如果显示您的广告位ID，说明配置成功！

---

**详细说明**：查看 `docs/环境变量配置指南.md`
