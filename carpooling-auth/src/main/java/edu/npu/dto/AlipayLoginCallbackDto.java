package edu.npu.dto;

public record AlipayLoginCallbackDto(
        String app_id,
        String source,
        String scope,
        String auth_code
) {
}
