package edu.npu.common;

/**
 * @author : [wangminan]
 * @description : [Redis在module中的常量]
 */
public class RedisConstants {

    public static final String SMS_CODE_PREFIX = "code:sms:";

    public static final String MAIL_CODE_PREFIX = "code:mail:";

    public static final Long CODE_EXPIRE_TIME = 60 * 5L;

    public static final String LOGIN_ACCOUNT_KEY_PREFIX = "login:account:";

    public static final Long LOGIN_ACCOUNT_EXPIRE_TTL = 180000000L;

    public static final String HASH_TOKEN_KEY = "token";

    public static final String HASH_LOGIN_ACCOUNT_KEY = "loginAccount";

    // 统计登录用户总数的KEY_PREFIX
    public static final String LOGIN_COUNT_KEY_PREFIX = "login:count:";

    public static final String LOGIN_LOCK_KEY = "login:lock";

    // 该统计过期时间为8天
    public static final Long LOGIN_COUNT_EXPIRE_TIME = 60 * 60 * 24 * 8L;
}
