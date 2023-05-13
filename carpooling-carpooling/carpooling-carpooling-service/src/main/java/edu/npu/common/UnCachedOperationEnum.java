package edu.npu.common;

import lombok.Getter;

@Getter
public enum UnCachedOperationEnum {

    INSERT(0),
    UPDATE(1),
    DELETE(2),
    SET(3);

    UnCachedOperationEnum(int value) {
        this.value = value;
    }

    private final int value;

    public static UnCachedOperationEnum fromValue(int value) {

        for (UnCachedOperationEnum e: UnCachedOperationEnum.values()) {
            if (value == e.getValue()) {
                return e;
            }
        }
        return null;
    }
}
