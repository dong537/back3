package com.example.demo.bazi.model;

import com.example.demo.bazi.constants.*;
import java.util.*;

/**
 * 八字命盘模型
 */
public class BaZiChart {
    private String id;
    private String baZiStr;        // 八字字符串，如 "乙酉 己丑 甲辰 戊辰"
    private String jiJie;          // 季节
    private String shengXiao;      // 生肖
    
    private Pillar nianZhu;        // 年柱
    private Pillar yueZhu;         // 月柱
    private Pillar riZhu;          // 日柱
    private Pillar shiZhu;         // 时柱
    
    private Map<String, String> xingYun;    // 星运（四柱十二长生）
    private Map<String, String> ziZuo;      // 自坐
    private Map<String, String> kongWang;   // 空亡
    private Map<String, List<String>> naYin; // 纳音
    
    private Map<String, Object> taiMingShen; // 胎元、命宫、身宫
    private Map<String, Object> yinYangFenXi; // 阴阳分析
    private String renYuanSiLing; // 人元司令（月令当值天干）
    private Map<String, Object> qiYunInfo; // 起运信息（年、月、日、小时）
    
    public BaZiChart() {
        this.xingYun = new LinkedHashMap<>();
        this.ziZuo = new LinkedHashMap<>();
        this.kongWang = new LinkedHashMap<>();
        this.naYin = new LinkedHashMap<>();
    }

    /**
     * 从八字字符串解析
     */
    public static BaZiChart parse(String baZiStr) {
        BaZiChart chart = new BaZiChart();
        chart.baZiStr = baZiStr;
        
        // 解析格式：乙酉 己丑 甲辰 戊辰
        String[] parts = baZiStr.split(" ");
        if (parts.length != 4) {
            throw new IllegalArgumentException("八字格式错误，应为：天干地支 天干地支 天干地支 天干地支");
        }
        
        // 解析四柱
        chart.nianZhu = parsePillar("年", parts[0]);
        chart.yueZhu = parsePillar("月", parts[1]);
        chart.riZhu = parsePillar("日", parts[2]);
        chart.shiZhu = parsePillar("时", parts[3]);
        
        // 设置生肖
        chart.shengXiao = DiZhi.fromName(chart.nianZhu.getDiZhi()).getShengXiao();
        
        // 计算十神
        String riZhuTianGan = chart.riZhu.getTianGan();
        chart.nianZhu.calculateShiShen(riZhuTianGan);
        chart.yueZhu.calculateShiShen(riZhuTianGan);
        chart.riZhu.calculateShiShen(riZhuTianGan);
        chart.shiZhu.calculateShiShen(riZhuTianGan);
        
        // 计算星运
        chart.nianZhu.calculateXingYun(riZhuTianGan);
        chart.yueZhu.calculateXingYun(riZhuTianGan);
        chart.riZhu.calculateXingYun(riZhuTianGan);
        chart.shiZhu.calculateXingYun(riZhuTianGan);
        
        chart.xingYun.put("年", chart.nianZhu.getXingYun());
        chart.xingYun.put("月", chart.yueZhu.getXingYun());
        chart.xingYun.put("日", chart.riZhu.getXingYun());
        chart.xingYun.put("时", chart.shiZhu.getXingYun());
        
        // 计算自坐
        chart.nianZhu.calculateZiZuo();
        chart.yueZhu.calculateZiZuo();
        chart.riZhu.calculateZiZuo();
        chart.shiZhu.calculateZiZuo();
        
        chart.ziZuo.put("年", chart.nianZhu.getZiZuo());
        chart.ziZuo.put("月", chart.yueZhu.getZiZuo());
        chart.ziZuo.put("日", chart.riZhu.getZiZuo());
        chart.ziZuo.put("时", chart.shiZhu.getZiZuo());
        
        // 计算空亡
        chart.kongWang.put("年", KongWang.getKongWangStr(chart.nianZhu.getGanZhi()));
        chart.kongWang.put("月", KongWang.getKongWangStr(chart.yueZhu.getGanZhi()));
        chart.kongWang.put("日", KongWang.getKongWangStr(chart.riZhu.getGanZhi()));
        chart.kongWang.put("时", KongWang.getKongWangStr(chart.shiZhu.getGanZhi()));
        
        // 纳音
        chart.naYin.put("年", Collections.singletonList(chart.nianZhu.getNaYin()));
        chart.naYin.put("月", Collections.singletonList(chart.yueZhu.getNaYin()));
        chart.naYin.put("日", Collections.singletonList(chart.riZhu.getNaYin()));
        chart.naYin.put("时", Collections.singletonList(chart.shiZhu.getNaYin()));
        
        // 确定季节
        chart.jiJie = determineJiJie(chart.yueZhu.getDiZhi());
        
        // 计算胎元命宫身宫
        chart.calculateTaiMingShen();
        
        // 计算阴阳分布
        chart.calculateYinYang();
        
        // 计算人元司令（月令当值天干，简化处理：取月支藏干的本气）
        chart.calculateRenYuanSiLing();
        
        // 生成ID
        chart.id = chart.baZiStr.replace(" ", "") + "1";
        
        return chart;
    }

    private static Pillar parsePillar(String name, String ganZhi) {
        if (ganZhi.length() != 2) {
            throw new IllegalArgumentException("干支格式错误: " + ganZhi);
        }
        String tianGan = ganZhi.substring(0, 1);
        String diZhi = ganZhi.substring(1, 2);
        return new Pillar(name, tianGan, diZhi);
    }

    private static String determineJiJie(String yueZhi) {
        switch (yueZhi) {
            case "寅": case "卯": case "辰": return "春";
            case "巳": case "午": case "未": return "夏";
            case "申": case "酉": case "戌": return "秋";
            case "亥": case "子": case "丑": return "冬";
            default: return "未知";
        }
    }

    /**
     * 计算胎元、命宫、身宫
     */
    private void calculateTaiMingShen() {
        this.taiMingShen = new LinkedHashMap<>();
        
        // 胎元：月干进一位 + 月支进三位
        TianGan yueGan = TianGan.fromName(yueZhu.getTianGan());
        DiZhi yueZhi = DiZhi.fromName(yueZhu.getDiZhi());
        
        TianGan taiYuanGan = TianGan.fromIndex(yueGan.getIndex() + 1);
        DiZhi taiYuanZhi = DiZhi.fromIndex(yueZhi.getIndex() + 3);
        String taiYuan = taiYuanGan.getName() + taiYuanZhi.getName();
        
        // 命宫计算（简化算法）
        int yueZhiIndex = yueZhi.getIndex();
        int shiZhiIndex = DiZhi.fromName(shiZhu.getDiZhi()).getIndex();
        int mingGongIndex = (14 - yueZhiIndex - shiZhiIndex + 12) % 12;
        if (mingGongIndex == 0) mingGongIndex = 12;
        DiZhi mingGongZhi = DiZhi.fromIndex(mingGongIndex);
        
        // 根据年干确定命宫天干
        TianGan nianGan = TianGan.fromName(nianZhu.getTianGan());
        int baseIndex = ((nianGan.getIndex() - 1) % 5) * 2 + 1;
        int mingGongGanIndex = (baseIndex + mingGongIndex - 1) % 10;
        if (mingGongGanIndex == 0) mingGongGanIndex = 10;
        TianGan mingGongGan = TianGan.fromIndex(mingGongGanIndex);
        String mingGong = mingGongGan.getName() + mingGongZhi.getName();
        
        // 身宫计算
        int shenGongIndex = (yueZhiIndex + shiZhiIndex - 2) % 12 + 1;
        DiZhi shenGongZhi = DiZhi.fromIndex(shenGongIndex);
        int shenGongGanIndex = (baseIndex + shenGongIndex - 1) % 10;
        if (shenGongGanIndex == 0) shenGongGanIndex = 10;
        TianGan shenGongGan = TianGan.fromIndex(shenGongGanIndex);
        String shenGong = shenGongGan.getName() + shenGongZhi.getName();
        
        taiMingShen.put("胎元", taiYuan);
        taiMingShen.put("命宫", mingGong);
        taiMingShen.put("身宫", shenGong);
        
        Map<String, String> naYinMap = new LinkedHashMap<>();
        naYinMap.put("胎元", NaYin.getNaYin(taiYuan));
        naYinMap.put("命宫", NaYin.getNaYin(mingGong));
        naYinMap.put("身宫", NaYin.getNaYin(shenGong));
        taiMingShen.put("纳音", naYinMap);
    }

    /**
     * 计算人元司令（月令当值天干）
     * 简化处理：取月支藏干的本气（第一个藏干）
     */
    private void calculateRenYuanSiLing() {
        DiZhi yueZhi = DiZhi.fromName(yueZhu.getDiZhi());
        String[] cangGan = yueZhi.getCangGan();
        if (cangGan.length > 0) {
            this.renYuanSiLing = cangGan[0];
        } else {
            this.renYuanSiLing = "";
        }
    }

    /**
     * 计算阴阳分布
     */
    private void calculateYinYang() {
        this.yinYangFenXi = new LinkedHashMap<>();
        int yangCount = 0;
        int yinCount = 0;
        
        // 统计天干
        if (TianGan.fromName(nianZhu.getTianGan()).isYang()) yangCount++; else yinCount++;
        if (TianGan.fromName(yueZhu.getTianGan()).isYang()) yangCount++; else yinCount++;
        if (TianGan.fromName(riZhu.getTianGan()).isYang()) yangCount++; else yinCount++;
        if (TianGan.fromName(shiZhu.getTianGan()).isYang()) yangCount++; else yinCount++;
        
        // 统计地支
        if (DiZhi.fromName(nianZhu.getDiZhi()).isYang()) yangCount++; else yinCount++;
        if (DiZhi.fromName(yueZhu.getDiZhi()).isYang()) yangCount++; else yinCount++;
        if (DiZhi.fromName(riZhu.getDiZhi()).isYang()) yangCount++; else yinCount++;
        if (DiZhi.fromName(shiZhu.getDiZhi()).isYang()) yangCount++; else yinCount++;
        
        Map<String, Integer> fenBu = new LinkedHashMap<>();
        fenBu.put("阳", yangCount);
        fenBu.put("阴", yinCount);
        yinYangFenXi.put("阴阳能量分布", fenBu);
    }

    /**
     * 获取日主天干
     */
    public String getRiZhuTianGan() {
        return riZhu.getTianGan();
    }

    /**
     * 获取日主五行
     */
    public String getRiZhuWuXing() {
        return riZhu.getTianGanWuXing();
    }

    /**
     * 获取所有四柱
     */
    public List<Pillar> getAllPillars() {
        return Arrays.asList(nianZhu, yueZhu, riZhu, shiZhu);
    }

    /**
     * 获取八字各柱信息Map
     */
    public Map<String, Map<String, Object>> getBaZiGeZhuInfo() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        result.put("年", nianZhu.toMap());
        result.put("月", yueZhu.toMap());
        result.put("日", riZhu.toMap());
        result.put("时", shiZhu.toMap());
        return result;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBaZiStr() { return baZiStr; }
    public void setBaZiStr(String baZiStr) { this.baZiStr = baZiStr; }
    public String getJiJie() { return jiJie; }
    public void setJiJie(String jiJie) { this.jiJie = jiJie; }
    public String getShengXiao() { return shengXiao; }
    public void setShengXiao(String shengXiao) { this.shengXiao = shengXiao; }
    public Pillar getNianZhu() { return nianZhu; }
    public void setNianZhu(Pillar nianZhu) { this.nianZhu = nianZhu; }
    public Pillar getYueZhu() { return yueZhu; }
    public void setYueZhu(Pillar yueZhu) { this.yueZhu = yueZhu; }
    public Pillar getRiZhu() { return riZhu; }
    public void setRiZhu(Pillar riZhu) { this.riZhu = riZhu; }
    public Pillar getShiZhu() { return shiZhu; }
    public void setShiZhu(Pillar shiZhu) { this.shiZhu = shiZhu; }
    public Map<String, String> getXingYun() { return xingYun; }
    public void setXingYun(Map<String, String> xingYun) { this.xingYun = xingYun; }
    public Map<String, String> getZiZuo() { return ziZuo; }
    public void setZiZuo(Map<String, String> ziZuo) { this.ziZuo = ziZuo; }
    public Map<String, String> getKongWang() { return kongWang; }
    public void setKongWang(Map<String, String> kongWang) { this.kongWang = kongWang; }
    public Map<String, List<String>> getNaYin() { return naYin; }
    public void setNaYin(Map<String, List<String>> naYin) { this.naYin = naYin; }
    public Map<String, Object> getTaiMingShen() { return taiMingShen; }
    public void setTaiMingShen(Map<String, Object> taiMingShen) { this.taiMingShen = taiMingShen; }
    public Map<String, Object> getYinYangFenXi() { return yinYangFenXi; }
    public void setYinYangFenXi(Map<String, Object> yinYangFenXi) { this.yinYangFenXi = yinYangFenXi; }
    public String getRenYuanSiLing() { return renYuanSiLing; }
    public void setRenYuanSiLing(String renYuanSiLing) { this.renYuanSiLing = renYuanSiLing; }
    public Map<String, Object> getQiYunInfo() { return qiYunInfo; }
    public void setQiYunInfo(Map<String, Object> qiYunInfo) { this.qiYunInfo = qiYunInfo; }
}
