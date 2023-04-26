package edu.npu.util;

import java.time.LocalDateTime;

public record RedisData(
        LocalDateTime expireTime,
        Object data
) {
}
