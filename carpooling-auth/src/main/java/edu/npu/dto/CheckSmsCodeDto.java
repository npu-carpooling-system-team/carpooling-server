package edu.npu.dto;

import edu.npu.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CheckSmsCodeDto(
        @NotNull
        @Pattern(regexp = RegexPatterns.PHONE_REGEX, message = "手机号格式不正确")
        String phone,

        @NotNull
        @Pattern(regexp = "[0-9]{4}", message = "验证码格式不正确")
        String code
) {
}
