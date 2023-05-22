package edu.npu.common;

import lombok.Getter;

/**
 * 用于表示订单状态的ENUM 比0大都是进行中
 * 小于等于0都是已结束
 * 0表示正常结束的一个订单
 */
@Getter
public enum OrderStatusEnum {
    // 订单强制结束
    ORDER_FORCE_CLOSED(-1),

    // 发车前阶段的订单状态
    PRE_ORDER_REQUEST_SUBMITTED(1),
    PRE_ORDER_REQUEST_PASSED(2),

    // 待发车阶段-用户取消订单 需要有一个不同的状态用于统计次数
    // 司机修改订单的情况已经写死了 不用改了
    PRE_DEPARTURE_USER_CANCELLED(3),

    // 行车阶段
    DRIVING_USER_CONFIRM_DEPARTURE(4),

    // 到达阶段
    ARRIVED_USER_UNPAID(6),

    // 乘客已支付 等待回调
    PAID_WAITING_CALLBACK(7),

    // 订单正常结束 乘客支付完成后自动进入评分并结束订单
    ORDER_NORMAL_CLOSED(0);

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
