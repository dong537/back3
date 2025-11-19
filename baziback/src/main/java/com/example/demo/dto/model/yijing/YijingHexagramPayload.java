package com.example.demo.dto.model.yijing;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代表 yijing_generate_hexagram 返回的完整卦象資料結構。
 * 將此類直接序列化即可滿足 yijing_interpret 接口對 hexagram 參數的要求。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingHexagramPayload {

    private String timestamp;
    private String method;
    private String question;
    private HexagramDetail original;

    @JsonProperty("changing_lines")
    private List<Integer> changingLines;

    private HexagramDetail changed;

    @JsonProperty("interpretation_hint")
    private String interpretationHint;

    /**
     * 卦象具體描述（本卦或變卦）
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HexagramDetail {
        private Integer id;
        private String binary;
        private List<HexagramLine> lines;
        private String name;
        private String chinese;
        private String upper;
        private String lower;
        private String symbol;
        private String judgment;
        private String image;
        private String meaning;
        private List<String> keywords;
        private String element;
        private String season;
        private String direction;
        private Map<String, String> applications;

        /**
         * 若服務端新增額外欄位，透過 extraFields 保存，避免反序列化失敗。
         */
        private Map<String, Object> extraFields;

        @JsonAnySetter
        public void setExtraField(String key, Object value) {
            if (extraFields == null) {
                extraFields = new HashMap<>();
            }
            extraFields.put(key, value);
        }
    }

    /**
     * 單條爻資訊。
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HexagramLine {
        private Integer position;
        private String type;
        private Boolean changing;
        private Integer value;
    }

    /**
     * 變爻的詳細說明。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChangingLineDetail extends HexagramLine {
        private String text;
        private String advice;
        private Map<String, Object> extra;

        @JsonAnySetter
        public void captureExtra(String key, Object value) {
            if (extra == null) {
                extra = new HashMap<>();
            }
            extra.put(key, value);
        }
    }

    /**
     * 允許捕捉頂層額外欄位，保持前向兼容。
     */
    private Map<String, Object> extraFields;

    @JsonAnySetter
    public void setExtraField(String key, Object value) {
        if (extraFields == null) {
            extraFields = new HashMap<>();
        }
        extraFields.put(key, value);
    }
}

