package com.example.demo.bazi.util;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 阳历日期转八字干支工具类（使用成熟节气/干支库，避免固定节气日期表导致的误差）
 *
 * 规则：
 * 1 年柱：以立春为界（库内实现）
 * 2 月柱：以节气为界（精确到分钟，库内实现）
 * 3 日柱：库内按历法计算
 * 4 时柱：子时 23:00-00:59（库内实现）
 * 5 夜子时规则：23:00-23:59 日柱按次日（本实现叠加）
 * 6 真太阳时换算（可选）：按经度修正时间： (longitude - 120) * 4 分钟
 *   - 120°E 为东八区中央经线
 *   - 经度东偏(>120) 真太阳时更早，经度西偏(<120) 真太阳时更晚
 */
public class DateToBaZiConverter {

    private static final DateTimeFormatter INPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分");

    /**
     * 默认：不做真太阳时换算
     */
    public static String convert(String dateTimeStr) {
        return convert(dateTimeStr, null);
    }

    /**
     * @param longitude 经度（东经为正），可为空。例：北京约 116.4
     */
    public static String convert(String dateTimeStr, Double longitude) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, INPUT_FMT);
        return convert(dateTime, longitude);
    }

    public static String convert(LocalDateTime dateTime) {
        return convert(dateTime, null);
    }

    public static String convert(LocalDateTime dateTime, Double longitude) {
        // 第一步：应用真太阳时换算
        LocalDateTime solarTime = applyTrueSolarTime(dateTime, longitude);
        
        // 第二步：计算月柱（基于真太阳时，不受夜子时影响）
        Solar solarForMonth = Solar.fromYmdHms(
                solarTime.getYear(),
                solarTime.getMonthValue(),
                solarTime.getDayOfMonth(),
                solarTime.getHour(),
                solarTime.getMinute(),
                solarTime.getSecond()
        );
        Lunar lunarForMonth = solarForMonth.getLunar();
        String monthPillar = lunarForMonth.getEightChar().getMonth();
        
        // 第三步：处理夜子时（只影响日柱和时柱）
        LocalDateTime adjusted = adjustForNightZiHour(solarTime);
        
        // 第四步：计算其他三柱
        Solar solar = Solar.fromYmdHms(
                adjusted.getYear(),
                adjusted.getMonthValue(),
                adjusted.getDayOfMonth(),
                adjusted.getHour(),
                adjusted.getMinute(),
                adjusted.getSecond()
        );
        Lunar lunar = solar.getLunar();
        var eightChar = lunar.getEightChar();

        return String.join(" ",
                eightChar.getYear(),
                monthPillar,  // 使用基于真太阳时的月柱
                eightChar.getDay(),
                eightChar.getTime()
        );
    }

    public static Map<String, Object> convertDetailed(String dateTimeStr, boolean isMale) {
        return convertDetailed(dateTimeStr, isMale, null);
    }

    public static Map<String, Object> convertDetailed(String dateTimeStr, boolean isMale, Double longitude) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, INPUT_FMT);
        return convertDetailed(dateTime, isMale, longitude);
    }

    public static Map<String, Object> convertDetailed(LocalDateTime dateTime, boolean isMale) {
        return convertDetailed(dateTime, isMale, null);
    }

    public static Map<String, Object> convertDetailed(LocalDateTime dateTime, boolean isMale, Double longitude) {
        // 第一步：应用真太阳时换算（如果提供经度）
        LocalDateTime solarTime = applyTrueSolarTime(dateTime, longitude);
        
        // 第二步：计算月柱（必须在夜子时调整之前，因为月柱以节气为界，不受夜子时影响）
        // 月柱的计算应该基于真太阳时，而不是调整后的时间
        Solar solarForMonth = Solar.fromYmdHms(
                solarTime.getYear(),
                solarTime.getMonthValue(),
                solarTime.getDayOfMonth(),
                solarTime.getHour(),
                solarTime.getMinute(),
                solarTime.getSecond()
        );
        Lunar lunarForMonth = solarForMonth.getLunar();
        String monthPillar = lunarForMonth.getEightChar().getMonth();
        
        // 第三步：处理夜子时（只影响日柱和时柱，不影响月柱）
        LocalDateTime adjusted = adjustForNightZiHour(solarTime);
        
        // 第四步：计算其他三柱（年柱、日柱、时柱）
        Solar solar = Solar.fromYmdHms(
                adjusted.getYear(),
                adjusted.getMonthValue(),
                adjusted.getDayOfMonth(),
                adjusted.getHour(),
                adjusted.getMinute(),
                adjusted.getSecond()
        );
        Lunar lunar = solar.getLunar();
        var eightChar = lunar.getEightChar();

        String yearPillar = eightChar.getYear();
        // 月柱使用基于真太阳时的计算结果，确保节气边界准确
        String dayPillar = eightChar.getDay();
        String hourPillar = eightChar.getTime();
        String baZi = String.join(" ", yearPillar, monthPillar, dayPillar, hourPillar);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("出生时间", dateTime.format(DISPLAY_FMT));

        if (longitude != null) {
            result.put("经度", round(longitude));
            result.put("真太阳时", solarTime.format(DISPLAY_FMT));
            result.put("真太阳时偏移分钟", calcTrueSolarOffsetMinutes(longitude));
        }

        result.put("计算用时间", adjusted.format(DISPLAY_FMT));
        if (solarTime.getHour() == 23) {
            result.put("夜子时说明", "23:00-23:59 视为次日（日柱按次日，但月柱仍按节气边界）");
        }

        result.put("八字", baZi);
        result.put("年柱", yearPillar);
        result.put("月柱", monthPillar);
        result.put("日柱", dayPillar);
        result.put("时柱", hourPillar);
        result.put("性别", isMale ? "男" : "女");

        // 生肖：以年柱地支推算
        String yearZhi = yearPillar.substring(1, 2);
        result.put("生肖", getShengXiaoByYearZhi(yearZhi));

        result.put("出生年份", dateTime.getYear());

        Map<String, Object> pillarsDetail = new LinkedHashMap<>();
        pillarsDetail.put("年", pillarDetail(yearPillar));
        pillarsDetail.put("月", pillarDetail(monthPillar));
        pillarsDetail.put("日", pillarDetail(dayPillar));
        pillarsDetail.put("时", pillarDetail(hourPillar));
        result.put("四柱详情", pillarsDetail);

        // 当前节气（用于解释月柱边界）- 使用真太阳时对应的节气
        result.put("节气", lunarForMonth.getJieQi());
        result.put("月柱计算说明", "月柱严格按照节气边界计算，基于真太阳时，不受夜子时调整影响");

        return result;
    }

    /**
     * 真太阳时换算
     * offsetMinutes = (longitude - 120) * 4
     */
    private static LocalDateTime applyTrueSolarTime(LocalDateTime dateTime, Double longitude) {
        if (longitude == null) {
            return dateTime;
        }
        int offsetMinutes = calcTrueSolarOffsetMinutes(longitude);
        return dateTime.plusMinutes(offsetMinutes);
    }

    private static int calcTrueSolarOffsetMinutes(Double longitude) {
        // 四舍五入到分钟
        return (int) Math.round((longitude - 120d) * 4d);
    }

    private static LocalDateTime adjustForNightZiHour(LocalDateTime dateTime) {
        // 夜子时：23:00-23:59 日柱按次日
        if (dateTime.getHour() == 23) {
            return dateTime.plusDays(1);
        }
        return dateTime;
    }

    private static Map<String, Object> pillarDetail(String ganZhi) {
        Map<String, Object> detail = new LinkedHashMap<>();
        if (ganZhi == null || ganZhi.length() < 2) return detail;
        String gan = ganZhi.substring(0, 1);
        String zhi = ganZhi.substring(1, 2);
        detail.put("天干", gan);
        detail.put("地支", zhi);

        detail.put("天干五行", com.nlf.calendar.util.LunarUtil.WU_XING_GAN.get(gan));
        detail.put("地支五行", com.nlf.calendar.util.LunarUtil.WU_XING_ZHI.get(zhi));
        detail.put("纳音", com.nlf.calendar.util.LunarUtil.NAYIN.get(ganZhi));
        return detail;
    }

    private static String getShengXiaoByYearZhi(String zhi) {
        return switch (zhi) {
            case "子" -> "鼠";
            case "丑" -> "牛";
            case "寅" -> "虎";
            case "卯" -> "兔";
            case "辰" -> "龙";
            case "巳" -> "蛇";
            case "午" -> "马";
            case "未" -> "羊";
            case "申" -> "猴";
            case "酉" -> "鸡";
            case "戌" -> "狗";
            case "亥" -> "猪";
            default -> "";
        };
    }

    private static double round(double v) {
        return new BigDecimal(v).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
