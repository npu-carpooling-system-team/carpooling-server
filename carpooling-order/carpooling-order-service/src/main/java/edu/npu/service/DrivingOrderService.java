package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.vo.R;

/**
 * 行车阶段业务处理类
 */
public interface DrivingOrderService extends IService<Order> {
    R passengerConfirmDeparture(Long orderId, LoginAccount loginAccount);

    R passengerConfirmArrived(Long orderId, LoginAccount loginAccount);
}
