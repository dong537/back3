package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卦爻表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TbHexagramYao {
    private Integer id;
    private Integer hexagramId;
    private Integer yaoPosition;
    private String yaoType;  // '阳' 或 '阴'
    private String stem;     // 纳干
    private String branch;   // 纳支
    private String liuQin;  // 六亲
    private Integer isShi;   // 是否世爻 0-否，1-是
    private Integer isYing;  // 是否应爻 0-否，1-是
}
