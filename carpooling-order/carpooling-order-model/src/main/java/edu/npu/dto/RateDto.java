package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record RateDto(
        @NotNull(message = "评分不得为空")
        int score
) {
}
