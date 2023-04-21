package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Order;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.PreDepartureOrderService;

/**
 * @author : [wangminan]
 * @description : [预定前业务实现类]
 */
public class PreDepartureOrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements PreDepartureOrderService {
}
