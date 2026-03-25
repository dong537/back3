# 移动端App适配文档

## 概述

本文档描述了针对移动端App（Android和iOS）的适配优化工作，确保应用在各种移动设备上都能提供良好的用户体验。

## 已完成的适配工作

### 1. Capacitor配置优化

**文件**: `capacitor.config.ts`

**优化内容**:
- ✅ 配置Android和iOS特定设置
- ✅ 设置启动画面和状态栏样式
- ✅ 配置键盘行为
- ✅ 设置背景颜色和安全区域

**关键配置**:
```typescript
- 状态栏样式：深色主题
- 启动画面：2秒自动隐藏
- 键盘：自动调整body大小
- 安全区域：自动适配
```

### 2. 安全区域支持

**实现位置**: `src-frontend/index.css`

**功能**:
- ✅ 支持iOS刘海屏（safe-area-inset-top）
- ✅ 支持底部安全区域（safe-area-inset-bottom）
- ✅ 支持左右安全区域（safe-area-inset-left/right）
- ✅ 兼容iOS 11.0-11.2（使用constant()）

**CSS类**:
- `.safe-area-top` - 顶部安全区域
- `.safe-area-bottom` - 底部安全区域
- `.safe-area-left` - 左侧安全区域
- `.safe-area-right` - 右侧安全区域

### 3. 移动端触摸优化

**实现位置**: `src-frontend/index.css` 和组件

**优化内容**:
- ✅ 触摸高亮颜色优化（tap-highlight）
- ✅ 最小触摸目标（44x44px，符合iOS和Android规范）
- ✅ 触摸反馈动画（active:scale-95）
- ✅ 防止文本选择（no-select类）

**CSS类**:
- `.tap-highlight` - 触摸高亮
- `.no-select` - 禁用文本选择
- `.touch-action-*` - 触摸行为控制

### 4. 响应式布局优化

**实现位置**: 各页面组件

**优化内容**:
- ✅ 移动端字体大小优化（14px基础）
- ✅ 网格布局响应式（移动端1-2列，桌面端3-5列）
- ✅ 卡片间距优化（移动端gap-4，桌面端gap-6）
- ✅ 按钮尺寸优化（移动端最小44px高度）

**断点**:
- 移动端: `< 640px`
- 平板: `640px - 768px`
- 桌面: `> 768px`

### 5. 移动端交互优化

**实现位置**: `src-frontend/utils/mobile.js`

**功能**:
- ✅ 检测移动设备类型（iOS/Android）
- ✅ 防止iOS双击缩放
- ✅ 设置视口高度（解决100vh问题）
- ✅ 震动反馈支持
- ✅ 触觉反馈

**工具函数**:
```javascript
- isMobile() - 检测是否为移动设备
- isIOS() - 检测是否为iOS
- isAndroid() - 检测是否为Android
- preventDoubleTapZoom() - 防止双击缩放
- setViewportHeight() - 设置视口高度
- vibrate() - 震动反馈
- hapticFeedback() - 触觉反馈
```

### 6. 组件移动端优化

#### 6.1 塔罗牌翻转卡片 (`TarotCardFlip.jsx`)
- ✅ 移动端触摸反馈
- ✅ 响应式尺寸
- ✅ 触摸缩放效果

#### 6.2 按钮组件 (`Button.jsx`)
- ✅ 最小触摸目标（44px）
- ✅ 触摸反馈动画
- ✅ 移动端优化

#### 6.3 浮动操作按钮 (`FloatingActionButton.jsx`)
- ✅ 安全区域适配
- ✅ 移动端触摸优化
- ✅ 响应式标签显示

#### 6.4 布局组件 (`Layout.jsx`)
- ✅ 顶部导航安全区域
- ✅ 底部安全区域
- ✅ 移动端菜单优化

### 7. HTML Meta标签优化

**文件**: `index.html`

**优化内容**:
- ✅ viewport-fit=cover（支持全屏显示）
- ✅ 禁用用户缩放（防止误操作）
- ✅ Apple Web App支持
- ✅ 主题颜色设置

## 移动端特定样式

### 输入框优化
```css
/* 防止iOS自动缩放 */
input, textarea, select {
  font-size: 16px;
}
```

### 滚动优化
```css
/* 平滑滚动 */
.scroll-smooth-mobile {
  -webkit-overflow-scrolling: touch;
  scroll-behavior: smooth;
}
```

### 性能优化
```css
/* 硬件加速 */
.will-change-transform {
  will-change: transform;
}
```

## 测试建议

### 设备测试
- [ ] iPhone (各种型号，包括刘海屏)
- [ ] Android手机 (各种尺寸)
- [ ] iPad / Android平板
- [ ] 横屏和竖屏模式

### 功能测试
- [ ] 安全区域显示正确
- [ ] 触摸反馈正常
- [ ] 翻转动画流畅
- [ ] 键盘弹出不影响布局
- [ ] 滚动流畅无卡顿
- [ ] 按钮点击区域足够大

### 性能测试
- [ ] 页面加载速度
- [ ] 动画帧率（60fps）
- [ ] 内存使用
- [ ] 电池消耗

## 已知问题和限制

1. **iOS橡皮筋效果**: 部分页面可能需要禁用橡皮筋效果
2. **键盘遮挡**: 某些输入框可能需要额外处理
3. **横屏适配**: 部分页面横屏显示可能需要优化

## 后续优化建议

1. **PWA支持**: 添加Service Worker和Manifest
2. **离线支持**: 缓存关键资源
3. **推送通知**: 集成推送通知功能
4. **深色模式**: 系统级深色模式支持
5. **手势支持**: 添加滑动手势
6. **性能监控**: 添加性能监控和错误追踪

## 构建和部署

### Android
```bash
npm run build
npm run cap:sync
npm run cap:open:android
```

### iOS
```bash
npm run build
npm run cap:sync
npm run cap:open:ios
```

## 注意事项

1. **安全区域**: 确保所有固定定位元素都考虑安全区域
2. **触摸目标**: 所有可点击元素至少44x44px
3. **字体大小**: 移动端使用16px以上防止自动缩放
4. **性能**: 避免过度使用动画和阴影
5. **测试**: 在真实设备上测试，模拟器可能不准确
