package edu.npu.dto;

import edu.npu.entity.Driver;
import edu.npu.entity.User;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
public record GetUserInfoDto(
    User user,
    Driver driver
) {
}
