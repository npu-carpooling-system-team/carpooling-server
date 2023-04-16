package edu.npu.dto;

import edu.npu.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserLoginDto(
        @NotNull
        @Pattern(regexp = RegexPatterns.PHONE_REGEX, message = "手机号格式不正确")
        String username,

        @NotNull
        String password
) {
}
