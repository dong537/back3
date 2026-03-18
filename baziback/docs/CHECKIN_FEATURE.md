# 连续打卡功能实现文档

## 功能概述

实现了连续打卡功能，用户在多次打卡之后可以获得更多的积分。系统会根据连续打卡天数给予不同的积分奖励，连续打卡3天和7天还有额外的奖励。

## 核心功能

### 1. 每日打卡限制
- **后端记录**：使用 `tb_daily_checkin` 表记录「用户ID + 日期」
- **唯一约束**：通过数据库唯一索引 `uk_user_date` 确保每个用户每天只能打卡一次
- **检查逻辑**：打卡前检查今天是否已打卡，如果已打卡则返回错误

### 2. 连续打卡天数计算
- **连续判断**：检查昨天是否打卡，如果昨天打卡了，今天继续打卡则连续天数+1
- **中断重置**：如果昨天没打卡，今天打卡则连续天数重置为1
- **首次打卡**：用户首次打卡，连续天数为1

### 3. 积分奖励规则

#### 基础奖励（根据连续天数）
- **第1-2天**：10积分/天
- **第3-6天**：20积分/天
- **第7天及以上**：30积分/天

#### 额外奖励（里程碑奖励）
- **连续3天**：额外奖励20积分
- **连续7天**：额外奖励50积分

#### 奖励示例
- 第1天：10积分
- 第2天：10积分
- 第3天：20积分（基础）+ 20积分（额外）= 40积分
- 第4天：20积分
- 第5天：20积分
- 第6天：20积分
- 第7天：30积分（基础）+ 50积分（额外）= 80积分
- 第8天及以后：30积分/天

## 数据库设计

### 表结构：`tb_daily_checkin`

```sql
CREATE TABLE `tb_daily_checkin` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `checkin_date` DATE NOT NULL COMMENT '签到日期',
    `checkin_time` DATETIME NOT NULL COMMENT '签到时间',
    `streak_days` INT NOT NULL DEFAULT 1 COMMENT '连续签到天数',
    `points_earned` INT NOT NULL COMMENT '本次签到获得的积分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 后端实现

### 1. Mapper：`DailyCheckinMapper`
- 位置：`src/main/java/com/example/demo/mapper/DailyCheckinMapper.java`
- 方法：
  - `findByUserIdAndDate()` - 根据用户ID和日期查询
  - `insert()` - 插入打卡记录
  - `findLastCheckinDate()` - 查询最近一次打卡日期
  - `findCurrentStreak()` - 查询当前连续天数
  - `findWeeklyCheckins()` - 查询本周打卡记录

### 2. Service：`CheckinService`
- 位置：`src/main/java/com/example/demo/service/CheckinService.java`
- 方法：
  - `doCheckin()` - 执行打卡（计算连续天数、奖励积分）
  - `getTodayStatus()` - 获取今日打卡状态
  - `getWeeklyProgress()` - 获取本周打卡进度
  - `getStreakInfo()` - 获取连续打卡信息

### 3. Controller：`CheckinController`
- 位置：`src/main/java/com/example/demo/controller/CheckinController.java`
- API接口：
  - `POST /api/checkin` - 执行打卡
  - `GET /api/checkin/today` - 获取今日打卡状态
  - `GET /api/checkin/weekly` - 获取本周打卡进度
  - `GET /api/checkin/streak` - 获取连续打卡信息

### 4. 积分服务：`CreditService`
- 完善了积分添加逻辑
- 更新 `tb_credit` 表余额
- 记录 `tb_credit_transaction` 流水

## 前端实现

### 1. API接口
- 位置：`src-frontend/api/index.js`
- 方法：
  - `checkinApi.doCheckin()` - 执行打卡
  - `checkinApi.getTodayStatus()` - 获取今日状态
  - `checkinApi.getWeeklyProgress()` - 获取本周进度
  - `checkinApi.getStreakInfo()` - 获取连续打卡信息

### 2. 组件

#### CheckinProgress（连续打卡奖励进度）
- 位置：`src-frontend/components/CheckinProgress.jsx`
- 功能：
  - 显示7天打卡进度（从本周一开始）
  - 显示第3天和第7天的特殊奖励
  - 显示当前连续打卡天数
  - 显示距离下一个奖励还需要多少天
  - 执行打卡操作

#### DailyCheckin（每日签到）
- 位置：`src-frontend/components/DailyCheckin.jsx`
- 功能：
  - 简化的打卡界面
  - 显示连续打卡天数
  - 显示今日奖励
  - 执行打卡操作

## 业务流程

### 场景1：用户首次打卡
1. 用户点击打卡按钮
2. 前端调用 `POST /api/checkin`
3. 后端检查今天是否已打卡（未打卡）
4. 计算连续天数（首次打卡，连续天数为1）
5. 计算奖励积分（10积分）
6. 保存打卡记录
7. 添加积分到用户账户
8. 返回结果给前端
9. 前端显示打卡成功和获得的积分

### 场景2：用户连续打卡
1. 用户昨天已打卡，今天继续打卡
2. 后端检查昨天是否打卡（已打卡）
3. 计算连续天数（昨天连续天数+1）
4. 根据新的连续天数计算奖励
5. 如果是第3天或第7天，添加额外奖励
6. 保存记录并添加积分

### 场景3：用户中断后重新打卡
1. 用户昨天没打卡，今天打卡
2. 后端检查昨天是否打卡（未打卡）
3. 连续天数重置为1
4. 按第1天的奖励计算（10积分）

### 场景4：用户今天已打卡
1. 用户尝试再次打卡
2. 后端检查今天是否已打卡（已打卡）
3. 返回错误："今天已经打卡过了，请明天再来"
4. 前端显示错误提示

## 数据流程

### 打卡结果数据结构

```json
{
  "success": true,
  "streakDays": 3,
  "baseReward": 20,
  "bonusReward": 20,
  "totalReward": 40,
  "checkinDate": "2026-01-12",
  "message": "连续打卡3天！获得40积分（含额外奖励）"
}
```

### 本周打卡进度数据结构

```json
{
  "weekStart": "2026-01-06",
  "weekEnd": "2026-01-12",
  "checkins": [
    {
      "id": 1,
      "userId": 1,
      "checkinDate": "2026-01-06",
      "streakDays": 1,
      "pointsEarned": 10
    },
    ...
  ],
  "weekStatus": [true, true, true, false, false, false, false]
}
```

## UI设计

### CheckinProgress组件特性
- **7天进度显示**：横向排列，显示本周一到本周日的打卡状态
- **奖励标识**：第3天和第7天显示特殊奖励标签
- **状态图标**：
  - 已打卡：绿色对勾
  - 今天未打卡：紫色脉冲动画
  - 未来日期：灰色
  - 奖励日：礼物图标
- **实时积分显示**：显示当前积分余额
- **奖励规则说明**：清晰展示奖励规则

## 安全机制

1. **用户认证**：所有API都需要JWT token验证
2. **唯一性保证**：数据库唯一索引防止重复打卡
3. **事务保护**：使用 `@Transactional` 确保数据一致性
4. **错误处理**：完善的异常处理和错误提示

## 测试建议

1. **正常流程测试**：
   - 用户首次打卡
   - 用户连续打卡（2天、3天、7天）
   - 用户中断后重新打卡
   - 用户查看打卡进度

2. **边界情况测试**：
   - 未登录用户尝试打卡
   - 用户尝试重复打卡
   - 跨周打卡（周一打卡，检查上周记录）
   - 跨月打卡

3. **积分计算测试**：
   - 验证第1-2天的奖励（10积分）
   - 验证第3天的奖励（20+20=40积分）
   - 验证第7天的奖励（30+50=80积分）
   - 验证第8天及以后的奖励（30积分）

## 部署步骤

1. **执行数据库脚本**（如果表不存在）：
   ```sql
   source database/add_referral_system_tables.sql
   ```

2. **重启后端服务**：确保新的Mapper和Service被加载

3. **前端无需额外配置**：API接口已自动包含认证token

## 注意事项

1. **时区问题**：确保服务器时区正确，`LocalDate.now()` 使用服务器时区
2. **并发控制**：虽然数据库唯一索引可以防止重复，但建议在高并发场景下考虑加锁
3. **数据清理**：可以考虑定期清理历史打卡记录（如保留最近90天）
4. **奖励调整**：奖励规则在 `CheckinService` 中配置，可以根据需要调整

## 后续优化建议

1. **打卡提醒**：每天提醒用户打卡
2. **打卡统计**：统计用户总打卡天数、最长连续天数等
3. **排行榜**：连续打卡天数排行榜
4. **特殊奖励**：连续30天、100天等里程碑奖励
5. **补签功能**：允许用户使用积分补签（可选）
