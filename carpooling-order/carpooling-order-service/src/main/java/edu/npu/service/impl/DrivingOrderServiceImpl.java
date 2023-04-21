package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Order;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.DrivingOrderService;

/**
 * @author : [wangminan]
 * @description : [行车阶段业务实现类]
 */
public class DrivingOrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements DrivingOrderService {
}
