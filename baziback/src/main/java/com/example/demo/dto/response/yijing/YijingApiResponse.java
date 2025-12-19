package com.example.demo.dto.response.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingApiResponse<T> {
    private boolean success;
    private T data;
    private String raw;
    private String errorMsg;

    public static <T> YijingApiResponse<T> success(T data) {
        return new YijingApiResponse<>(true, data, null, null);
    }

    public static <T> YijingApiResponse<T> failure(String errorMsg) {
        return new YijingApiResponse<>(false, null, null, errorMsg);
    }
}
