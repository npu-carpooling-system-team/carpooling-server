package edu.npu.exception;

import edu.npu.common.ResponseCodeEnum;
import edu.npu.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CarpoolingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleCarpoolingException(CarpoolingException e){
        log.error("CarpoolingException:{}", e.getMessage());

        return R.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)//此方法捕获Exception异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//状态码返回500
    public R doException(Exception e){

        log.error("捕获异常：{}",e.getMessage());

        if(e.getMessage().equals("不允许访问")){
            return R.error("没有操作此功能的权限");
        }
        return R.error(ResponseCodeEnum.SERVER_ERROR, e.getMessage());
    }
}
