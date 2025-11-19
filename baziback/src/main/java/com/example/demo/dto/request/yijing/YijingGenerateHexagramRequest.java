package com.example.demo.dto.request.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingGenerateHexagramRequest {

    @NotBlank(message = "question 不能为空")
    private String question;

    /**
     * 起卦方式，例如：number、time、plum_blossom、random、coin（根据服务端文档扩展）
     */
    @NotBlank(message = "method 不能为空")
    private String method;

    /**
     * 起卦种子（可选）
     */
    private String seed;

    private String context;
}

