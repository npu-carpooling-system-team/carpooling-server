package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record AddMessageDto(
        @NotNull
        Long toUserId,
        @NotNull
        String message
) {
}
