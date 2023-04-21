package edu.npu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Builder
public record ToUserVo(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,
        String username,
        String avatar
) {
}
