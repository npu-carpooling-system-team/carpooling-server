package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Date;

public record AddCarpoolingDto(

    @NotNull
    String arrivePoint,
    @NotNull
    String departurePoint,
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    Date arriveTime,
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    Date departureTime,
    @NotNull
    String description,
    @NotNull
    Integer leftPassengerNo,
    @NotNull
    String passingPoint,
    @NotNull(message = "price不能为空")
    Integer price,
    @NotNull
    Integer totalPassengerNo
) {
}
