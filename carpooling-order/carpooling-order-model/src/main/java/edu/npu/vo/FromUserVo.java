package edu.npu.vo;

import lombok.Builder;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Builder
public record FromUserVo(
        Long id,
        String username,
        String avatar
) {
}
