package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.service.PreOrderService;
import edu.npu.mapper.OrderMapper;
import edu.npu.vo.R;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【order(拼车订单表)】的数据库操作Service实现
* @createDate 2023-04-20 14:21:40
*/
@Service
public class PreOrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements PreOrderService {

    @Override
    public R passengerApply(Integer carpoolingId, LoginAccount loginAccount) {
        return null;
    }
}




