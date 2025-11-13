package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 格式化后的八字响应（前端友好的格式）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormattedBaziResponse {

    // ===== 基本信息 =====
    private String gender;           // 性别
    private String solarDate;        // 阳历
    private String lunarDate;        // 农历
    private String bazi;             // 八字（如：戊寅 己未 己卯 辛未）
    private String zodiac;           // 生肖
    private String dayMaster;        // 日主

    // ===== 四柱信息 =====
    private PillarInfo yearPillar;   // 年柱
    private PillarInfo monthPillar;  // 月柱
    private PillarInfo dayPillar;    // 日柱
    private PillarInfo hourPillar;   // 时柱

    // ===== 其他信息 =====
    private String taiyuan;          // 胎元
    private String taixi;            // 胎息
    private String minggong;         // 命宫
    private String shengong;         // 身宫

    // ===== 大运信息 =====
    private DayunInfo dayun;         // 大运

    // ===== 神煞信息 =====
    private Map<String, List<String>> shensha;  // 神煞

    // ===== 刑冲合会 =====
    private Map<String, Object> xingChongHeHui; // 刑冲合会

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSolarDate() {
        return solarDate;
    }

    public void setSolarDate(String solarDate) {
        this.solarDate = solarDate;
    }

    public String getLunarDate() {
        return lunarDate;
    }

    public void setLunarDate(String lunarDate) {
        this.lunarDate = lunarDate;
    }

    public String getBazi() {
        return bazi;
    }

    public void setBazi(String bazi) {
        this.bazi = bazi;
    }

    public String getZodiac() {
        return zodiac;
    }

    public void setZodiac(String zodiac) {
        this.zodiac = zodiac;
    }

    public String getDayMaster() {
        return dayMaster;
    }

    public void setDayMaster(String dayMaster) {
        this.dayMaster = dayMaster;
    }

    public PillarInfo getYearPillar() {
        return yearPillar;
    }

    public void setYearPillar(PillarInfo yearPillar) {
        this.yearPillar = yearPillar;
    }

    public PillarInfo getMonthPillar() {
        return monthPillar;
    }

    public void setMonthPillar(PillarInfo monthPillar) {
        this.monthPillar = monthPillar;
    }

    public PillarInfo getDayPillar() {
        return dayPillar;
    }

    public void setDayPillar(PillarInfo dayPillar) {
        this.dayPillar = dayPillar;
    }

    public PillarInfo getHourPillar() {
        return hourPillar;
    }

    public void setHourPillar(PillarInfo hourPillar) {
        this.hourPillar = hourPillar;
    }

    public String getTaiyuan() {
        return taiyuan;
    }

    public void setTaiyuan(String taiyuan) {
        this.taiyuan = taiyuan;
    }

    public String getTaixi() {
        return taixi;
    }

    public void setTaixi(String taixi) {
        this.taixi = taixi;
    }

    public String getMinggong() {
        return minggong;
    }

    public void setMinggong(String minggong) {
        this.minggong = minggong;
    }

    public String getShengong() {
        return shengong;
    }

    public void setShengong(String shengong) {
        this.shengong = shengong;
    }

    public DayunInfo getDayun() {
        return dayun;
    }

    public void setDayun(DayunInfo dayun) {
        this.dayun = dayun;
    }

    public Map<String, List<String>> getShensha() {
        return shensha;
    }

    public void setShensha(Map<String, List<String>> shensha) {
        this.shensha = shensha;
    }

    public Map<String, Object> getXingChongHeHui() {
        return xingChongHeHui;
    }

    public void setXingChongHeHui(Map<String, Object> xingChongHeHui) {
        this.xingChongHeHui = xingChongHeHui;
    }

    /**
     * 柱信息
     */
    @Data
    public static class PillarInfo {
        private TianganInfo tiangan;      // 天干
        private DizhiInfo dizhi;          // 地支
        private String nayin;             // 纳音
        private String xun;               // 旬
        private String kongwang;          // 空亡
        private String xingyun;           // 星运
        private String zizuo;             // 自坐

        public TianganInfo getTiangan() {
            return tiangan;
        }

        public void setTiangan(TianganInfo tiangan) {
            this.tiangan = tiangan;
        }

        public DizhiInfo getDizhi() {
            return dizhi;
        }

        public void setDizhi(DizhiInfo dizhi) {
            this.dizhi = dizhi;
        }

        public String getNayin() {
            return nayin;
        }

        public void setNayin(String nayin) {
            this.nayin = nayin;
        }

        public String getXun() {
            return xun;
        }

        public void setXun(String xun) {
            this.xun = xun;
        }

        public String getKongwang() {
            return kongwang;
        }

        public void setKongwang(String kongwang) {
            this.kongwang = kongwang;
        }

        public String getXingyun() {
            return xingyun;
        }

        public void setXingyun(String xingyun) {
            this.xingyun = xingyun;
        }

        public String getZizuo() {
            return zizuo;
        }

        public void setZizuo(String zizuo) {
            this.zizuo = zizuo;
        }
    }

    /**
     * 天干信息
     */
    @Data
    public static class TianganInfo {
        private String tiangan;           // 天干
        private String wuxing;            // 五行
        private String yinyang;           // 阴阳
        private String shishen;           // 十神

        public String getTiangan() {
            return tiangan;
        }

        public void setTiangan(String tiangan) {
            this.tiangan = tiangan;
        }

        public String getWuxing() {
            return wuxing;
        }

        public void setWuxing(String wuxing) {
            this.wuxing = wuxing;
        }

        public String getYinyang() {
            return yinyang;
        }

        public void setYinyang(String yinyang) {
            this.yinyang = yinyang;
        }

        public String getShishen() {
            return shishen;
        }

        public void setShishen(String shishen) {
            this.shishen = shishen;
        }
    }

    /**
     * 地支信息
     */
    @Data
    public static class DizhiInfo {
        private String dizhi;             // 地支
        private String wuxing;            // 五行
        private String yinyang;           // 阴阳
        private CangganInfo canggan;      // 藏干

        public String getDizhi() {
            return dizhi;
        }

        public void setDizhi(String dizhi) {
            this.dizhi = dizhi;
        }

        public String getWuxing() {
            return wuxing;
        }

        public void setWuxing(String wuxing) {
            this.wuxing = wuxing;
        }

        public String getYinyang() {
            return yinyang;
        }

        public void setYinyang(String yinyang) {
            this.yinyang = yinyang;
        }

        public CangganInfo getCanggan() {
            return canggan;
        }

        public void setCanggan(CangganInfo canggan) {
            this.canggan = canggan;
        }
    }

    /**
     * 藏干信息
     */
    @Data
    public static class CangganInfo {
        private GanInfo zhuqi;            // 主气
        private GanInfo zhongqi;          // 中气
        private GanInfo yuqi;             // 余气

        public GanInfo getZhuqi() {
            return zhuqi;
        }

        public void setZhuqi(GanInfo zhuqi) {
            this.zhuqi = zhuqi;
        }

        public GanInfo getZhongqi() {
            return zhongqi;
        }

        public void setZhongqi(GanInfo zhongqi) {
            this.zhongqi = zhongqi;
        }

        public GanInfo getYuqi() {
            return yuqi;
        }

        public void setYuqi(GanInfo yuqi) {
            this.yuqi = yuqi;
        }
    }

    /**
     * 干信息（藏干中的单个干）
     */
    @Data
    public static class GanInfo {
        private String tiangan;           // 天干
        private String shishen;           // 十神

        public String getTiangan() {
            return tiangan;
        }

        public void setTiangan(String tiangan) {
            this.tiangan = tiangan;
        }

        public String getShishen() {
            return shishen;
        }

        public void setShishen(String shishen) {
            this.shishen = shishen;
        }
    }

    /**
     * 大运信息
     */
    @Data
    public static class DayunInfo {
        private String qiyunDate;         // 起运日期
        private Integer qiyunAge;         // 起运年龄
        private List<DayunPeriod> periods; // 大运周期列表

        public String getQiyunDate() {
            return qiyunDate;
        }

        public void setQiyunDate(String qiyunDate) {
            this.qiyunDate = qiyunDate;
        }

        public Integer getQiyunAge() {
            return qiyunAge;
        }

        public void setQiyunAge(Integer qiyunAge) {
            this.qiyunAge = qiyunAge;
        }

        public List<DayunPeriod> getPeriods() {
            return periods;
        }

        public void setPeriods(List<DayunPeriod> periods) {
            this.periods = periods;
        }
    }

    /**
     * 大运周期
     */
    @Data
    public static class DayunPeriod {
        private String ganzhi;            // 干支（如：庚申）
        private Integer startYear;        // 开始年份
        private Integer endYear;          // 结束年份
        private String tianganShishen;    // 天干十神
        private List<String> dizhiShishen; // 地支十神
        private List<String> dizhiCanggan; // 地支藏干
        private Integer startAge;         // 开始年龄
        private Integer endAge;           // 结束年龄

        public String getGanzhi() {
            return ganzhi;
        }

        public void setGanzhi(String ganzhi) {
            this.ganzhi = ganzhi;
        }

        public Integer getStartYear() {
            return startYear;
        }

        public void setStartYear(Integer startYear) {
            this.startYear = startYear;
        }

        public Integer getEndYear() {
            return endYear;
        }

        public void setEndYear(Integer endYear) {
            this.endYear = endYear;
        }

        public String getTianganShishen() {
            return tianganShishen;
        }

        public void setTianganShishen(String tianganShishen) {
            this.tianganShishen = tianganShishen;
        }

        public List<String> getDizhiShishen() {
            return dizhiShishen;
        }

        public void setDizhiShishen(List<String> dizhiShishen) {
            this.dizhiShishen = dizhiShishen;
        }

        public List<String> getDizhiCanggan() {
            return dizhiCanggan;
        }

        public void setDizhiCanggan(List<String> dizhiCanggan) {
            this.dizhiCanggan = dizhiCanggan;
        }

        public Integer getStartAge() {
            return startAge;
        }

        public void setStartAge(Integer startAge) {
            this.startAge = startAge;
        }

        public Integer getEndAge() {
            return endAge;
        }

        public void setEndAge(Integer endAge) {
            this.endAge = endAge;
        }
    }

    /**
     * 从原始响应转换
     */
    @SuppressWarnings("unchecked")
    public static FormattedBaziResponse fromMcpResponse(McpBaziResponse mcpResponse) {
        if (mcpResponse == null || mcpResponse.getBaziData() == null) {
            return null;
        }

        try {
            FormattedBaziResponse response = new FormattedBaziResponse();
            Map<String, Object> data = mcpResponse.getBaziData();

            // 基本信息
            response.setGender(safeGetString(data, "性别"));
            response.setSolarDate(safeGetString(data, "阳历"));
            response.setLunarDate(safeGetString(data, "农历"));
            response.setBazi(safeGetString(data, "八字"));
            response.setZodiac(safeGetString(data, "生肖"));
            response.setDayMaster(safeGetString(data, "日主"));

            // 其他信息
            response.setTaiyuan(safeGetString(data, "胎元"));
            response.setTaixi(safeGetString(data, "胎息"));
            response.setMinggong(safeGetString(data, "命宫"));
            response.setShengong(safeGetString(data, "身宫"));

            // 四柱信息 - 完整解析（使用try-catch确保单个柱解析失败不影响其他）
            try {
                Map<String, Object> yearPillarMap = safeGetMap(data, "年柱");
                if (yearPillarMap != null && !yearPillarMap.isEmpty()) {
                    response.setYearPillar(parsePillar(yearPillarMap));
                }
            } catch (Exception e) {
                System.err.println("解析年柱失败: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                Map<String, Object> monthPillarMap = safeGetMap(data, "月柱");
                if (monthPillarMap != null && !monthPillarMap.isEmpty()) {
                    response.setMonthPillar(parsePillar(monthPillarMap));
                }
            } catch (Exception e) {
                System.err.println("解析月柱失败: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                Map<String, Object> dayPillarMap = safeGetMap(data, "日柱");
                if (dayPillarMap != null && !dayPillarMap.isEmpty()) {
                    response.setDayPillar(parsePillar(dayPillarMap));
                } else {
                    System.err.println("警告: 日柱数据为空或不存在");
                }
            } catch (Exception e) {
                System.err.println("解析日柱失败: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                Map<String, Object> hourPillarMap = safeGetMap(data, "时柱");
                if (hourPillarMap != null && !hourPillarMap.isEmpty()) {
                    response.setHourPillar(parsePillar(hourPillarMap));
                } else {
                    System.err.println("警告: 时柱数据为空或不存在");
                }
            } catch (Exception e) {
                System.err.println("解析时柱失败: " + e.getMessage());
                e.printStackTrace();
            }

            // 大运信息
            try {
                response.setDayun(parseDayun(safeGetMap(data, "大运")));
            } catch (Exception e) {
                // 记录错误但继续处理
            }

            // 神煞
            try {
                response.setShensha((Map<String, List<String>>) data.get("神煞"));
            } catch (Exception e) {
                // 记录错误但继续处理
            }

            // 刑冲合会
            try {
                response.setXingChongHeHui((Map<String, Object>) data.get("刑冲合会"));
            } catch (Exception e) {
                // 记录错误但继续处理
            }

            return response;
        } catch (Exception e) {
            // 如果整体转换失败，返回null
            return null;
        }
    }

    /**
     * 安全获取字符串值
     */
    private static String safeGetString(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 安全获取Map值
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> safeGetMap(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    /**
     * 解析柱信息
     */
    @SuppressWarnings("unchecked")
    private static PillarInfo parsePillar(Map<String, Object> pillarMap) {
        if (pillarMap == null || pillarMap.isEmpty()) {
            return null;
        }

        PillarInfo pillar = new PillarInfo();

        // 天干信息
        Map<String, Object> tianganMap = (Map<String, Object>) pillarMap.get("天干");
        if (tianganMap != null) {
            TianganInfo tiangan = new TianganInfo();
            tiangan.setTiangan((String) tianganMap.get("天干"));
            tiangan.setWuxing((String) tianganMap.get("五行"));
            tiangan.setYinyang((String) tianganMap.get("阴阳"));
            tiangan.setShishen((String) tianganMap.get("十神"));
            pillar.setTiangan(tiangan);
        }

        // 地支信息
        Map<String, Object> dizhiMap = (Map<String, Object>) pillarMap.get("地支");
        if (dizhiMap != null) {
            DizhiInfo dizhi = new DizhiInfo();
            dizhi.setDizhi((String) dizhiMap.get("地支"));
            dizhi.setWuxing((String) dizhiMap.get("五行"));
            dizhi.setYinyang((String) dizhiMap.get("阴阳"));

            // 藏干信息
            Map<String, Object> cangganMap = (Map<String, Object>) dizhiMap.get("藏干");
            if (cangganMap != null) {
                CangganInfo canggan = new CangganInfo();
                canggan.setZhuqi(parseGanInfo((Map<String, Object>) cangganMap.get("主气")));
                canggan.setZhongqi(parseGanInfo((Map<String, Object>) cangganMap.get("中气")));
                canggan.setYuqi(parseGanInfo((Map<String, Object>) cangganMap.get("余气")));
                dizhi.setCanggan(canggan);
            }

            pillar.setDizhi(dizhi);
        }

        // 其他信息
        pillar.setNayin((String) pillarMap.get("纳音"));
        pillar.setXun((String) pillarMap.get("旬"));
        pillar.setKongwang((String) pillarMap.get("空亡"));
        pillar.setXingyun((String) pillarMap.get("星运"));
        pillar.setZizuo((String) pillarMap.get("自坐"));

        return pillar;
    }

    /**
     * 解析干信息（藏干中的单个干）
     */
    private static GanInfo parseGanInfo(Map<String, Object> ganMap) {
        if (ganMap == null || ganMap.isEmpty()) {
            return null;
        }

        GanInfo gan = new GanInfo();
        gan.setTiangan((String) ganMap.get("天干"));
        gan.setShishen((String) ganMap.get("十神"));
        return gan;
    }

    /**
     * 解析大运信息
     */
    @SuppressWarnings("unchecked")
    private static DayunInfo parseDayun(Map<String, Object> dayunMap) {
        if (dayunMap == null || dayunMap.isEmpty()) {
            return null;
        }

        DayunInfo dayun = new DayunInfo();
        dayun.setQiyunDate((String) dayunMap.get("起运日期"));
        
        Object qiyunAgeObj = dayunMap.get("起运年龄");
        if (qiyunAgeObj instanceof Integer) {
            dayun.setQiyunAge((Integer) qiyunAgeObj);
        } else if (qiyunAgeObj instanceof Number) {
            dayun.setQiyunAge(((Number) qiyunAgeObj).intValue());
        }

        // 解析大运周期列表
        List<Map<String, Object>> periodsList = (List<Map<String, Object>>) dayunMap.get("大运");
        if (periodsList != null && !periodsList.isEmpty()) {
            List<DayunPeriod> periods = new ArrayList<>();
            for (Map<String, Object> periodMap : periodsList) {
                DayunPeriod period = new DayunPeriod();
                period.setGanzhi((String) periodMap.get("干支"));
                
                Object startYearObj = periodMap.get("开始年份");
                if (startYearObj instanceof Integer) {
                    period.setStartYear((Integer) startYearObj);
                } else if (startYearObj instanceof Number) {
                    period.setStartYear(((Number) startYearObj).intValue());
                }
                
                Object endYearObj = periodMap.get("结束");
                if (endYearObj instanceof Integer) {
                    period.setEndYear((Integer) endYearObj);
                } else if (endYearObj instanceof Number) {
                    period.setEndYear(((Number) endYearObj).intValue());
                }
                
                period.setTianganShishen((String) periodMap.get("天干十神"));
                period.setDizhiShishen((List<String>) periodMap.get("地支十神"));
                period.setDizhiCanggan((List<String>) periodMap.get("地支藏干"));
                
                Object startAgeObj = periodMap.get("开始年龄");
                if (startAgeObj instanceof Integer) {
                    period.setStartAge((Integer) startAgeObj);
                } else if (startAgeObj instanceof Number) {
                    period.setStartAge(((Number) startAgeObj).intValue());
                }
                
                Object endAgeObj = periodMap.get("结束年龄");
                if (endAgeObj instanceof Integer) {
                    period.setEndAge((Integer) endAgeObj);
                } else if (endAgeObj instanceof Number) {
                    period.setEndAge(((Number) endAgeObj).intValue());
                }
                
                periods.add(period);
            }
            dayun.setPeriods(periods);
        }

        return dayun;
    }
}