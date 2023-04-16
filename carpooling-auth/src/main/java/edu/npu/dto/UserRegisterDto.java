package edu.npu.dto;

import edu.npu.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * @author : [wangminan]
 * @description : [用户注册的数据传输对象]
 */
public record UserRegisterDto(
        @NotNull
        @Pattern(regexp = RegexPatterns.PHONE_REGEX, message = "手机号格式不正确")
        String username,
        @NotNull
        @Pattern(regexp = RegexPatterns.PASSWORD_REGEX, message = "密码格式不正确")
        String password,
        @Pattern(regexp = RegexPatterns.EMAIL_REGEX, message = "邮箱格式不正确")
        String email,
        @NotNull
        boolean isDriver,
        @NotNull
        boolean isPassenger,
        String driversName,
        @Pattern(regexp = RegexPatterns.ID_CARD_REGEX, message = "身份证号格式不正确")
        String driversPersonalId,
        String driversLicenseId,
        String driversLicenseType,
        @Pattern(regexp = RegexPatterns.PLATE_NO_REGEX, message = "车牌号格式不正确")
        String driversPlateNo,
        String driversVehicleType,
        String driversExpireDate
) {

}
