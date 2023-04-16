package edu.npu.dto;

import edu.npu.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SendMailDto(
        @NotNull
        @Pattern(regexp = RegexPatterns.EMAIL_REGEX, message = "邮箱格式不正确")
        String email
) {
}
