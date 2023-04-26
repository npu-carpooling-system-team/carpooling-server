package edu.npu.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum {

    // ==================== 200 OK ====================
    SUCCESS(2000),

    // ==================== 400 BadRequest ====================
    REQUEST_ERROR(4000),

    CREATION_ERROR(4001),

    // ==================== 401 Unauthorized ====================
    ACCESS_TOKEN_EXPIRED_ERROR(4010),

    USER_UNAUTHENTICATED(4012),

    // ==================== 403 Forbidden ====================
    FORBIDDEN(4030),

    NOT_ENOUGH_INFORMATION(4031),

    PRE_CHECK_FAILED(4032),

    // ==================== 404 NotFound ====================
    NOT_FOUND(4040),

    // ==================== 500 InternalServerError ====================
    SERVER_ERROR(5000);

    private final int value;

    public HttpStatus getStatus() {

        int httpCode = value / 10;
        return HttpStatus.valueOf(httpCode);
    }
}
