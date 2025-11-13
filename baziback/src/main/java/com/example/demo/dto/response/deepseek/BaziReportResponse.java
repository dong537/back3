package com.example.demo.dto.response.deepseek;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 八字报告生成响应类（标准化返回格式）
 */
@Data
public class BaziReportResponse {
    /**
     * 响应码：200=成功，500=系统异常，400=参数错误，401=API密钥无效，429=限流
     */
    private Integer code;

    /**
     * 响应消息：成功/失败描述
     */
    private String message;

    /**
     * 核心数据：AI生成的八字命理报告（成功时返回，失败时为null）
     */
    private String reportContent;

    /**
     * 追踪ID：唯一标识一次请求，便于日志排查
     */
    private String traceId;

    /**
     * 报告生成时间（成功时返回）
     */
    private LocalDateTime generateTime;

    // ==================== 静态工厂方法（简化对象创建） ====================

    /**
     * 成功响应（带报告内容）
     * @param reportContent AI生成的八字报告
     * @return 成功响应对象
     */
    public static BaziReportResponse success(String reportContent) {
        BaziReportResponse response = new BaziReportResponse();
        response.setCode(200);
        response.setMessage("八字报告生成成功");
        response.setReportContent(reportContent);
        response.setTraceId(UUID.randomUUID().toString().replace("-", "")); // 生成唯一追踪ID
        response.setGenerateTime(LocalDateTime.now()); // 自动填充当前时间
        return response;
    }

    /**
     * 失败响应（通用异常）
     * @param message 失败描述
     * @return 失败响应对象
     */
    public static BaziReportResponse fail(String message) {
        BaziReportResponse response = new BaziReportResponse();
        response.setCode(500);
        response.setMessage(message);
        response.setReportContent(null);
        response.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        response.setGenerateTime(LocalDateTime.now());
        return response;
    }

    /**
     * 失败响应（指定错误码）
     * @param code 错误码（如400=参数错误，401=密钥无效）
     * @param message 失败描述
     * @return 失败响应对象
     */
    public static BaziReportResponse fail(Integer code, String message) {
        BaziReportResponse response = new BaziReportResponse();
        response.setCode(code);
        response.setMessage(message);
        response.setReportContent(null);
        response.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        response.setGenerateTime(LocalDateTime.now());
        return response;
    }
}