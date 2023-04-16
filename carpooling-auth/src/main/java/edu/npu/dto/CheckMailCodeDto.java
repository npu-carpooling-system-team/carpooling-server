package edu.npu.dto;

import edu.npu.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
public record CheckMailCodeDto(
        @NotNull
        @Pattern(regexp = RegexPatterns.EMAIL_REGEX, message = "邮箱格式不正确")
        String mail,

        @NotNull
        @Pattern(regexp = "[0-9]{4}", message = "验证码格式不正确")
        String code
) {
}
