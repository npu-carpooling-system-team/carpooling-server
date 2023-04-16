package edu.npu.util;

/**
 * @author wangminan
 */
public abstract class RegexPatterns {
    /**
     * 手机号正则
     */
    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
    /**
     * 邮箱正则
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * 验证码正则, 4位数字
     */
    public static final String VERIFY_CODE_REGEX = "^[\\d]{4}$";
    /**
     * 身份证正则。用于校验中国身份证号是否合法
     */
    public static final String ID_CARD_REGEX = "^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$";
    /**
     * 车牌号正则。用于校验车牌号是否合法，包括中国燃油车与新能源车。
     */
    public static final String PLATE_NO_REGEX = "^[\\u4e00-\\u9fa5]{1}[A-Z]{1}[A-Z_0-9]{5,6}$";

}
