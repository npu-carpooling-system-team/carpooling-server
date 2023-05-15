package edu.npu.common;

/**
 * @author : [wangminan]
 * @description : [Redis在module中的常量]
 */
public class RedisConstants {

    private RedisConstants(){
        throw new IllegalStateException("Utility class");
    }

    public static final String SMS_CODE_PREFIX = "code:sms:";

    public static final String MAIL_CODE_PREFIX = "code:mail:";

    public static final Long CODE_EXPIRE_TIME = 60 * 5L;

    public static final String TOKEN_KEY_PREFIX = "token:";

    public static final Long TOKEN_EXPIRE_TTL = 180000000L;

    public static final String HASH_TOKEN_KEY = "token";

    public static final String HASH_LOGIN_ACCOUNT_KEY = "loginAccount";

//    public static final Long CACHE_NULL_TTL = 2L;

    public static final String CACHE_CARPOOLING_KEY = "carpooling:";

    public static final Long CACHE_CARPOOLING_TTL = 30L;

    public static final String LOCK_CARPOOLING_KEY = "lock:carpooling:";
}
