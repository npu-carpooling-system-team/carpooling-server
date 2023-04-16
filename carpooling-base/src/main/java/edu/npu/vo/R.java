package edu.npu.vo;

import edu.npu.common.ResponseCodeEnum;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : [wangminan]
 * @description : [统一返回结果]
 */
public class R extends HashMap<String, Object> {

    @Serial
    private static final long serialVersionUID = 16545151L;

    public R() {
        put("code", ResponseCodeEnum.Success.getValue());
    }

    public static R error() {
        return error(
                ResponseCodeEnum.ServerError, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(
                ResponseCodeEnum.ServerError, msg);
    }

    public static R error(ResponseCodeEnum responseCodeEnum, String msg) {
        R r = new R();
        r.put("code", responseCodeEnum.getValue());
        r.put("msg", msg);
        return r;
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.put("result", map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
