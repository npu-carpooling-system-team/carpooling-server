package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record GetCarpoolingDto(

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long id,
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
    @NotNull
    Integer price,
    @NotNull
    Integer totalPassengerNo
) {
}
