package edu.npu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author wangminan
 * @description 用户角色枚举
 */
@Getter
public enum RoleEnum {

    @JsonProperty("User")
    USER(0),


    @JsonProperty("Admin")
    ADMIN(1);

    RoleEnum(int value) {
        this.value = value;
    }

    public static RoleEnum fromValue(int value) {

        for (RoleEnum e: RoleEnum.values()) {
            if (value == e.getValue()) {
                return e;
            }
        }
        return null;
    }

    private final int value;

}

