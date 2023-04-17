package edu.npu.dto;

import edu.npu.util.RegexPatterns;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
public record PutUserInfoDto(

    String alipayId,
    String avgScore,
    String driverExpireDate,
    String driverName,
    String driverPersonalId,
    String driversLicenseNo,
    String driversLicenseType,
    String driversPlateNo,
    String driversVehicleType,
    @Pattern(regexp = RegexPatterns.EMAIL_REGEX, message = "邮箱格式不正确")
    String email,
    @NotNull
    Boolean isDriver,
    @NotNull
    Boolean isPassenger,
    String userImage,
    @NotNull
    @Pattern(regexp = RegexPatterns.PHONE_REGEX, message = "手机号格式不正确")
    String username
) {
}
