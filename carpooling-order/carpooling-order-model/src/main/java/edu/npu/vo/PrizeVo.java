package edu.npu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author : [wangminan]
 * @description : [奖励表中的字段名]
 */
public record PrizeVo(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long driverId,
        String driversName,
        String driverPhone,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long totalOrders
) {
}
