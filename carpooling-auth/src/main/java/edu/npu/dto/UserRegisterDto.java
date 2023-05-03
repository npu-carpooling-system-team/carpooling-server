package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        String email,
        @NotNull
        Boolean isDriver,
        @NotNull
        Boolean isPassenger,
        String driversName,
        String driversPersonalId,
        String driversLicenseNo,
        String driversLicenseType,
        String driversPlateNo,
        String driversVehicleType,
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        String driversExpireDate
) {

}
