package edu.npu.common;

/**
 * @author : [wangminan]
 * @description : [order模块使用到的Redis常量]
 */
public class RedisConstants {

    public static final String MESSAGE_NOTICE_KEY_PREFIX = "communication:notice:";

    public static final String UPLOAD_FILE_KEY_PREFIX = "upload:file:list";

    // 两天
    public static final Long UPLOAD_FILE_KEY_EXPIRE = 2 * 24 * 60 * 60L;
}
