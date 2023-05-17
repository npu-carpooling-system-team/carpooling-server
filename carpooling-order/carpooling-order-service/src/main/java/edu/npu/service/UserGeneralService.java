package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.vo.R;

public interface UserGeneralService extends IService<Order> {
    R getOrderList(LoginAccount loginAccount);

    R getOrderById(Long orderId);
}
