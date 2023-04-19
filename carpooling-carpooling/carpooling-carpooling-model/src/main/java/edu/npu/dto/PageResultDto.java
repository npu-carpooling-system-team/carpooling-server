package edu.npu.dto;

import edu.npu.doc.CarpoolingDoc;

import java.util.List;

public record PageResultDto(
        Long total,
        List<CarpoolingDoc> data
) {
}
