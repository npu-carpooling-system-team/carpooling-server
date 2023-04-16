package edu.npu.config;

import java.io.Serializable;

/**
 * 错误响应参数包装
 */
public class RestErrorResponse implements Serializable {

    private String msg;

    public RestErrorResponse(String msg){
        this.msg = msg;
    }

    public String getErrMessage() {
        return msg;
    }

    public void setErrMessage(String errMessage) {
        this.msg = errMessage;
    }
}
