package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
public record PassOrderDto(
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long carpoolingId,
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long passengerId,
        @NotNull
        Boolean pass

) {
}
