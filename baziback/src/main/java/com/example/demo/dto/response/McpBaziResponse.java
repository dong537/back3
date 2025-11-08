package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpBaziResponse {

    /**
     * 八字原始文本（JSON 字符串）
     */
    private String baziText;

    /**
     * ✅ 新增：解析后的结构化八字数据
     * 包含完整的八字信息：性别、阳历、农历、八字、生肖、四柱、大运等
     */
    private Map<String, Object> baziData;

    /**
     * MCP 原始响应（用于调试）
     */
    private String rawResponse;

    // ===== 便捷访问方法 =====

    /**
     * 获取性别
     */
    public String getGender() {
        return baziData != null ? (String) baziData.get("性别") : null;
    }

    /**
     * 获取阳历日期
     */
    public String getSolarDate() {
        return baziData != null ? (String) baziData.get("阳历") : null;
    }

    /**
     * 获取农历日期
     */
    public String getLunarDate() {
        return baziData != null ? (String) baziData.get("农历") : null;
    }

    /**
     * 获取八字
     */
    public String getBazi() {
        return baziData != null ? (String) baziData.get("八字") : null;
    }

    /**
     * 获取生肖
     */
    public String getZodiac() {
        return baziData != null ? (String) baziData.get("生肖") : null;
    }

    /**
     * 获取日主
     */
    public String getDayMaster() {
        return baziData != null ? (String) baziData.get("日主") : null;
    }

    /**
     * 获取年柱
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getYearPillar() {
        return baziData != null ? (Map<String, Object>) baziData.get("年柱") : null;
    }

    /**
     * 获取月柱
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMonthPillar() {
        return baziData != null ? (Map<String, Object>) baziData.get("月柱") : null;
    }

    /**
     * 获取日柱
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDayPillar() {
        return baziData != null ? (Map<String, Object>) baziData.get("日柱") : null;
    }

    /**
     * 获取时柱
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getHourPillar() {
        return baziData != null ? (Map<String, Object>) baziData.get("时柱") : null;
    }

    /**
     * 获取大运
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDayun() {
        return baziData != null ? (Map<String, Object>) baziData.get("大运") : null;
    }

    /**
     * 获取神煞
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getShensha() {
        return baziData != null ? (Map<String, Object>) baziData.get("神煞") : null;
    }

    /**
     * 获取刑冲合会
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getXingChongHeHui() {
        return baziData != null ? (Map<String, Object>) baziData.get("刑冲合会") : null;
    }
}