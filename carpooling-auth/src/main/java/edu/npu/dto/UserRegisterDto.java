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
        // 非空即可 密码经过RSA加密
        @NotNull
        String password,
        @Pattern(regexp = RegexPatterns.EMAIL_REGEX, message = "邮箱格式不正确")
        String email,
        @NotNull
        Boolean isDriver,
        @NotNull
        Boolean isPassenger,
        String driversName,
        @Pattern(regexp = RegexPatterns.ID_CARD_REGEX, message = "身份证号格式不正确")
        String driversPersonalId,
        String driversLicenseNo,
        String driversLicenseType,
        @Pattern(regexp = RegexPatterns.PLATE_NO_REGEX, message = "车牌号格式不正确")
        String driversPlateNo,
        String driversVehicleType,
        String driversExpireDate
) {

}
