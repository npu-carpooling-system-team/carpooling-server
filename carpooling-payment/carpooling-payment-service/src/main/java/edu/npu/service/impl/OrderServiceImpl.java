package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Order;
import edu.npu.service.OrderService;
import edu.npu.mapper.OrderMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【order(拼车订单表)】的数据库操作Service实现
* @createDate 2023-04-25 11:27:12
*/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements OrderService{

}




