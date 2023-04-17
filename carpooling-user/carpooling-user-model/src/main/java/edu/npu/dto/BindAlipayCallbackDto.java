package edu.npu.dto;

public record BindAlipayCallbackDto(
    String app_id,
    String source,
    String scope,
    String auth_code,
    String state
) {
}
