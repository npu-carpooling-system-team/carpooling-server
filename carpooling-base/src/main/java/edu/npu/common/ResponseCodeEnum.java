package edu.npu.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum {

    // ==================== 200 OK ====================
    Success(2000),

    // ==================== 400 BadRequest ====================
    RequestError(4000),

    CreationError(4001),

    // ==================== 401 Unauthorized ====================
    AccessTokenExpiredError(4010),

    UserUnauthenticated(4012),

    // ==================== 403 Forbidden ====================
    Forbidden(4030),

    NotEnoughInformation(4031),

    PreCheckFailed(4032),

    // ==================== 404 NotFound ====================
    NotFound(4040),

    // ==================== 500 InternalServerError ====================
    ServerError(5000);

    private final int value;

    public HttpStatus getStatus() {

        int httpCode = value / 10;
        return HttpStatus.valueOf(httpCode);
    }
}
