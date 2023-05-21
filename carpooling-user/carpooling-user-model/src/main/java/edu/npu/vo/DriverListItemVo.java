package edu.npu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record DriverListItemVo(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long driverId,
        String username,
        String driversName
) {
}
