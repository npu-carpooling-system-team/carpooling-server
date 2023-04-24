package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.vo.R;

/**
 * 发车前业务处理
 */
public interface PreDepartureOrderService extends IService<Order> {

    R userCancelOrder(Long orderId, LoginAccount loginAccount);

    R searchUserCancelTimes(LoginAccount loginAccount);
}
