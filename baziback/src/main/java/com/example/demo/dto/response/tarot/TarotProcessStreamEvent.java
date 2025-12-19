package com.example.demo.dto.response.tarot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TarotProcessStreamEvent {
    /**
     * step | draw | result | error
     */
    private String type;

    /**
     * Human readable message for step/draw/error.
     */
    private String message;

    /**
     * Present when type=draw or type=result.
     */
    private TarotDrawResultResponse draw;

    /**
     * Present when type=result.
     */
    private PerformReadingResponse result;

    public static TarotProcessStreamEvent step(String message) {
        return new TarotProcessStreamEvent("step", message, null, null);
    }

    public static TarotProcessStreamEvent draw(String message, TarotDrawResultResponse draw) {
        return new TarotProcessStreamEvent("draw", message, draw, null);
    }

    public static TarotProcessStreamEvent result(TarotDrawResultResponse draw, PerformReadingResponse result) {
        return new TarotProcessStreamEvent("result", null, draw, result);
    }

    public static TarotProcessStreamEvent error(String message) {
        return new TarotProcessStreamEvent("error", message, null, null);
    }
}
