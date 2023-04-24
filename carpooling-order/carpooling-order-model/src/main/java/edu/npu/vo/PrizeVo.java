package edu.npu.vo;

/**
 * @author : [wangminan]
 * @description : [奖励表中的字段名]
 */
public record PrizeVo(
        Long driverId,
        String driversName,
        String driverPhone,
        Long totalOrders
) {
}
