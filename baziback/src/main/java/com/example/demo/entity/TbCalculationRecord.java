package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User calculation history record.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbCalculationRecord {
    private Long id;
    private Long userId;
    private String recordType;
    private String recordTitle;
    private String question;
    private String summary;
    private String inputData;
    /**
     * Backward-compatible field name for the JSON result payload.
     * Stored in the database column `result_data`.
     */
    private String data;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
