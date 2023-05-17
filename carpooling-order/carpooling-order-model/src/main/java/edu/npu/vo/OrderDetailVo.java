package edu.npu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.npu.entity.Order;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Builder
public record OrderDetailVo(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long carpoolingId,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long passengerId,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        Date createTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        Date updateTime,
        Integer score,
        String departurePoint,
        String arrivePoint,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
        Date departureTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
        Date arriveTime
) {
}
