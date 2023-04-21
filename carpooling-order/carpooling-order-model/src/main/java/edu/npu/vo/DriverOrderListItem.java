package edu.npu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [司机查看某一行程申请情况时获取的数据]
 */
@Builder
public record DriverOrderListItem(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long orderId,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long passengerId,
        String passengerName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        Date applyTime
) {
}
