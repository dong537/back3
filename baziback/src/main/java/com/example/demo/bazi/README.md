# 八字测算系统 (BaZi Fortune-Telling System)

## 项目结构

```
bazi/
├── constants/          # 常量定义
│   ├── TianGan.java       # 天干枚举
│   ├── DiZhi.java         # 地支枚举
│   ├── WuXing.java        # 五行枚举
│   ├── ShiShen.java       # 十神枚举
│   ├── NaYin.java         # 纳音常量
│   ├── ShiErChangSheng.java # 十二长生
│   ├── KongWang.java      # 空亡常量
│   └── ShenSha.java       # 神煞常量
├── model/              # 数据模型
│   ├── Pillar.java        # 柱模型（年/月/日/时柱）
│   ├── BaZiChart.java     # 八字命盘模型
│   └── DaYun.java         # 大运模型
├── analyzer/           # 分析器
│   ├── CaiXingAnalyzer.java       # 财星分析器
│   ├── FuQiXingAnalyzer.java      # 夫妻星分析器
│   ├── FuMuXingAnalyzer.java      # 父母星分析器
│   ├── ZiNvXingAnalyzer.java      # 子女星分析器
│   ├── XingChongHeHuiAnalyzer.java # 刑冲合会分析器
│   ├── ShenShaAnalyzer.java       # 神煞分析器
│   ├── TiaoHouAnalyzer.java       # 调候分析器
│   ├── XiYongShenAnalyzer.java    # 喜用神分析器
│   ├── DaYunAnalyzer.java         # 大运分析器
│   ├── RiZhuAnalyzer.java         # 日柱分析器
│   └── ShiShenLunMingAnalyzer.java # 十神论命分析器
├── service/            # 服务层
│   ├── BaZiService.java       # 八字测算主服务
│   └── BaZiDeepSeekService.java # DeepSeek AI解读服务
├── controller/         # 控制器
│   └── BaZiController.java # REST API控制器
└── dto/                # 数据传输对象
    └── BaZiRequest.java   # 请求DTO
```

## 功能特性

### 1. 基础信息解析
- 八字四柱解析（年柱、月柱、日柱、时柱）
- 天干地支信息
- 藏干分析
- 十神计算

### 2. 命理分析
- **星运分析**: 十二长生状态（长生、沐浴、冠带等）
- **自坐分析**: 天干在本支的状态
- **空亡分析**: 各柱的空亡地支
- **纳音分析**: 六十甲子纳音

### 3. 六亲分析
- **财星信息**: 正财、偏财分布及财库
- **夫妻星信息**: 男命财星/女命官杀
- **父母星信息**: 正印(母)、偏财(父)
- **子女星信息**: 男命官杀/女命食伤

### 4. 关系分析
- **刑冲合会**: 天干合、地支六合、三合、三会、刑、冲、破、害

### 5. 神煞分析
- 天乙贵人、文昌贵人、驿马、桃花
- 华盖、将星、天德、月德
- 羊刃、飞刃、金舆
- 十恶大败、十灵、天罗地网等

### 6. 调候分析
- 调候用神推荐
- 湿燥等级判断

### 7. 喜用神分析
- 日主强弱判断
- 调候推荐
- 格局推荐
- 综合推荐

### 8. 大运分析
- 十步大运计算
- 大运干支十神分析

## API接口

### 完整八字分析
```
POST /api/bazi/analyze
Content-Type: application/json

{
    "baZi": "乙酉 己丑 甲辰 戊辰",
    "birthYear": 2005,
    "isMale": true
}
```

### 简易八字分析
```
POST /api/bazi/analyze/simple
Content-Type: application/json

{
    "baZi": "乙酉 己丑 甲辰 戊辰",
    "isMale": true
}
```

### GET方式分析
```
GET /api/bazi/analyze?baZi=乙酉 己丑 甲辰 戊辰&birthYear=2005&isMale=true
```

### 分析大运
```
POST /api/bazi/dayun
Content-Type: application/json

{
    "baZi": "乙酉 己丑 甲辰 戊辰",
    "birthYear": 2005,
    "isMale": true
}
```

### 分析刑冲合会
```
POST /api/bazi/xingchonghe
Content-Type: application/json

{
    "baZi": "乙酉 己丑 甲辰 戊辰"
}
```

### 分析神煞
```
POST /api/bazi/shensha
Content-Type: application/json

{
    "baZi": "乙酉 己丑 甲辰 戊辰"
}
```

### 分析喜用神
```
POST /api/bazi/xiyongshen
Content-Type: application/json

{
    "baZi": "乙酉 己丑 甲辰 戊辰"
}
```

### DeepSeek AI解读报告
```
POST /api/bazi/generate-report
Content-Type: application/json

{
    "baZi": "乙酉 己丑 甲辰 戊辰",
    "birthYear": 2005,
    "isMale": true
}
```

### DeepSeek AI直接解读
```
POST /api/bazi/interpret
Content-Type: text/plain

这是一段八字数据或提示词...
```

### DeepSeek AI特定方面解读
```
POST /api/bazi/interpret-aspects
Content-Type: application/json

{
    "baZi": "乙酉 己丑 甲辰 戊辰",
    "birthYear": 2005,
    "isMale": true,
    "aspects": ["事业", "财运", "婚姻", "健康"]
}
```

## 使用示例

```java
// 创建服务实例
BaZiService service = new BaZiService();

// 完整分析
Map<String, Object> result = service.analyze("乙酉 己丑 甲辰 戊辰", 2005, true);

// 简易分析
Map<String, Object> simple = service.analyzeSimple("乙酉 己丑 甲辰 戊辰", true);

// 仅分析大运
Map<String, Object> daYun = service.analyzeDaYun("乙酉 己丑 甲辰 戊辰", 2005, true);

// 仅分析喜用神
Map<String, Object> xiYong = service.analyzeXiYongShen("乙酉 己丑 甲辰 戊辰");
```

## 输出示例

分析结果包含以下主要字段：
- `_id`: 唯一标识
- `季节`: 出生季节
- `生肖`: 生肖
- `八字`: 八字字符串
- `星运`: 四柱十二长生状态
- `自坐`: 各柱自坐状态
- `空亡`: 各柱空亡
- `纳音`: 各柱纳音
- `胎命身`: 胎元、命宫、身宫
- `八字各柱信息`: 详细的四柱信息
- `财星信息`: 财星分布
- `夫妻星信息`: 夫妻星分布
- `父母星信息`: 父母星分布
- `子女星信息`: 子女星分布
- `阴阳情况分析`: 阴阳能量分布
- `刑冲合会`: 刑冲合会关系
- `神煞`: 各柱神煞
- `调候信息`: 调候分析
- `日柱等级信息`: 日柱等级和特点
- `喜用神分析`: 喜用神推荐
- `四柱十神论命知识`: 论命知识点
- `参考格局信息`: 格局判断
- `大运数据`: 十步大运

## 技术说明

- 基于 Spring Boot 框架
- 使用 Java 8+ 特性
- RESTful API 设计
- 支持跨域请求
