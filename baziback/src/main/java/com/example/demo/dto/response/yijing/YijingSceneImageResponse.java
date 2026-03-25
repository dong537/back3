package com.example.demo.dto.response.yijing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YijingSceneImageResponse {

    private String provider;

    private String model;

    @JsonProperty("scene_category")
    private String sceneCategory;

    private String prompt;

    @JsonProperty("revised_prompt")
    private String revisedPrompt;

    @JsonProperty("image_base64")
    private String imageBase64;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("generation_mode")
    private String generationMode;

    @JsonProperty("image_supported")
    private Boolean imageSupported;

    @JsonProperty("visual_summary")
    private String visualSummary;

    @JsonProperty("negative_prompt")
    private String negativePrompt;

    @JsonProperty("display_text")
    private String displayText;
}
