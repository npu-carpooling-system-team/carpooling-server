package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

public record AddMessageDto(
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long toUserId,
        @NotNull
        String message
) {
}
