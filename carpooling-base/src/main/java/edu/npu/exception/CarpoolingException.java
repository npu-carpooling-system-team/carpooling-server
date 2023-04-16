package edu.npu.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author : [wangminan]
 * @description : [自定义异常]
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CarpoolingException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 165421815L;

    private String msg;

    public CarpoolingException() {
        super();
    }

    public CarpoolingException(String message) {
        super(message);
        this.msg = message;
    }

    public static void cast(CarpoolingError commonError){
        throw new CarpoolingException(commonError.getErrMessage());
    }


    public static void cast(String errMessage){
        throw new CarpoolingException(errMessage);
    }

    public static void cast(CarpoolingError commonError, String errMessage){
        throw new CarpoolingException(commonError.getErrMessage() + " " +  errMessage);
    }

}
