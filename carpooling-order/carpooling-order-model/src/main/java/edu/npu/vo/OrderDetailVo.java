package edu.npu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.npu.entity.Order;

import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
public record OrderDetailVo(
        Order order,
        String departurePoint,
        String arrivePoint,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
        Date departureTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
        Date arriveTime
) {
}
