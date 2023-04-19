package edu.npu.vo;

import edu.npu.doc.CarpoolingDoc;

import java.util.List;

public record PageResultVo(
        Long total,
        List<CarpoolingDoc> data
) {
}
