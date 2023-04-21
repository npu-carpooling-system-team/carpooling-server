package edu.npu.common;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    // 订单强制结束
    Order_ForceClosed(0),

    // 发车前阶段的订单状态
    PreOrder_RequestSubmitted(1),
    PreOrder_RequestPassed(2);

    private final int value;

    OrderStatusEnum(int value) {
        this.value = value;
    }

    public static OrderStatusEnum fromValue(int value) {

        for (OrderStatusEnum e: OrderStatusEnum.values()) {
            if (value == e.getValue()) {
                return e;
            }
        }
        return null;
    }
}
