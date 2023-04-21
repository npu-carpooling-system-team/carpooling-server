package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Order;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.ArriveOrderStatus;

/**
 * @author : [wangminan]
 * @description : [到达后订单状态处理类]
 */
public class ArriveOrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements ArriveOrderStatus {
}
