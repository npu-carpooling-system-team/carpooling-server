package edu.npu.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : [wangminan]
 * @description : [支付宝请求参数常量]
 */
@AllArgsConstructor
@Getter
public enum AlipayRequestConstants {

    OUT_TRADE_NUMBER("out_trade_no");

    /**
     * 类型
     */
    private final String type;
}
