# 塔罗牌算命系统 - 全新设计

## 🎴 系统概述

这是一个基于Spring Boot的全新塔罗牌算命系统，提供了完整的塔罗牌解读、个人档案管理、学习资源等全方位功能。

## ✨ 主要功能

### 1. 塔罗解读功能
- **多种牌阵支持**：
  - 单张牌阵 (single_card) - 快速解答
  - 三张牌阵 (three_card) - 过去、现在、未来
  - 凯尔特十字牌阵 (celtic_cross) - 全面深入分析
  - 马蹄牌阵 (horseshoe) - 选择与结果
  - 关系十字牌阵 (relationship_cross) - 人际关系分析
  - 职业道路牌阵 (career_path) - 事业发展指引
  - 决策牌阵 (decision_making) - 重要抉择
  - 灵性指引牌阵 (spiritual_guidance) - 精神成长
  - 年度运势牌阵 (year_ahead) - 年度预测
  - 脉轮对齐牌阵 (chakra_alignment) - 能量平衡
  - 阴影工作牌阵 (shadow_work) - 内在探索
  - 每日运势牌阵 (daily_fortune) - 今日指引
  - 爱情塔罗牌阵 (love_tarot) - 感情分析
  - 事业塔罗牌阵 (career_tarot) - 职业发展
  - 健康塔罗牌阵 (health_tarot) - 身心健康

### 2. 个人档案管理
- 创建个性化塔罗档案
- 偏好设置（牌组、经验等级、解读风格）
- 阅读历史统计
- 成就系统

### 3. 收藏功能
- 收藏喜爱的牌面
- 添加个人备注
- 分类管理
- 使用频率统计

### 4. 学习资源
- 体系化学习路径
- 牌面含义详解
- 牌阵教学
- 解读技巧指导

### 5. 高级功能
- 自定义牌阵创建
- 牌面相似性搜索
- 数据库分析
- 智能牌阵推荐

## 🚀 API 接口

### 基础牌面功能
```
GET  /api/tarot/card/list          - 列出所有塔罗牌
GET  /api/tarot/card/info          - 获取单张牌信息
GET  /api/tarot/card/search        - 搜索塔罗牌
GET  /api/tarot/card/similar       - 查找相似牌
GET  /api/tarot/card/random        - 获取随机牌
```

### 塔罗解读功能
```
POST /api/tarot/reading/perform    - 执行塔罗解读
GET  /api/tarot/reading/history    - 获取解读历史
GET  /api/tarot/reading/{id}/detail - 获取解读详情
POST /api/tarot/reading/daily-fortune - 每日运势
```

### 个人档案管理
```
POST /api/tarot/profile/create     - 创建塔罗档案
GET  /api/tarot/profile/{userId}   - 获取塔罗档案
PUT  /api/tarot/profile/{userId}   - 更新塔罗档案
```

### 收藏功能
```
POST /api/tarot/favorites/manage   - 管理收藏牌
GET  /api/tarot/favorites/{userId} - 获取收藏牌
```

### 学习资源
```
POST /api/tarot/learning/resource  - 获取学习资源
GET  /api/tarot/learning/progress/{userId} - 获取学习进度
```

### 高级功能
```
POST /api/tarot/spread/custom      - 创建自定义牌阵
GET  /api/tarot/spread/recommend/{type} - 获取牌阵推荐
POST /api/tarot/analytics          - 获取数据库分析
```

## 📊 数据结构

### 解读请求示例
```json
{
  "spreadType": "three_card",
  "question": "我的感情发展如何？",
  "focusArea": "love",
  "includeReversed": true,
  "readingStyle": "intuitive",
  "timeFrame": 3,
  "userId": 123
}
```

### 解读响应示例
```json
{
  "success": true,
  "data": {
    "spreadType": "three_card",
    "question": "我的感情发展如何？",
    "cards": [
      {
        "name": "恋人",
        "orientation": "正位",
        "position": "过去",
        "generalMeaning": "关系建立、选择、和谐",
        "personalMeaning": "过去曾有过重要的感情经历",
        "keywords": ["爱情", "选择", "和谐"]
      }
    ],
    "overallInterpretation": "整体感情运势向好...",
    "advice": ["保持开放的心态", "相信直觉"],
    "timeFrame": "3个月内",
    "energyLevel": "积极",
    "readingId": "reading_123456"
  }
}
```

## 🛠️ 技术架构

### 后端技术栈
- **Spring Boot 2.x** - 主框架
- **Spring WebFlux** - 响应式Web框架
- **MyBatis** - 数据访问层
- **MySQL** - 数据库
- **MCP (Model Context Protocol)** - AI模型通信协议

### 核心组件
- `TarotController` - API控制器
- `TarotService` - 业务逻辑层
- `McpTarotClient` - MCP客户端
- `TarotReadingMapper` - 数据访问层

### 配置管理
- `application.yml` - 主配置文件
- `tarot-learning.properties` - 学习资源配置
- `TarotConfig` - 配置类

## 🎯 使用指南

### 1. 环境准备
```bash
# 克隆项目
git clone <repository-url>
cd tarot-system

# 配置数据库
CREATE DATABASE tarot_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 配置环境变量
export DB_URL=jdbc:mysql://localhost:3306/tarot_db
export DB_USERNAME=root
export DB_PASSWORD=your_password
export MCP_TAROT_KEY=your_api_key
```

### 2. 启动服务
```bash
./mvnw spring-boot:run
```

### 3. API 调用示例
```bash
# 获取每日运势
curl -X POST http://localhost:8088/api/tarot/reading/daily-fortune \
  -H "Content-Type: application/json" \
  -d '{"question": "今日运势", "userId": 1}'
```

## 📈 扩展计划

### 短期目标
- [ ] 添加更多牌阵类型
- [ ] 实现牌面图片生成功能
- [ ] 增加用户社区功能
- [ ] 支持多语言界面

### 长期规划
- [ ] AI 辅助解读
- [ ] 移动端应用开发
- [ ] 数据分析和统计
- [ ] 商业化功能扩展

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个项目！

### 开发规范
1. 遵循现有的代码风格
2. 为新功能编写单元测试
3. 更新相关文档
4. 确保向后兼容性

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

如有问题或建议，请通过以下方式联系：
- 邮箱：contact@tarot-system.com
- 微信公众号：塔罗牌算命系统

---

*"愿塔罗牌指引你找到内心的答案"* ✨
