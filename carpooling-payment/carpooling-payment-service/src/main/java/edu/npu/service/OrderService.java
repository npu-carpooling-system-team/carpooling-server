package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.vo.R;

import java.util.Map;

/**
* @author wangminan
* @description 针对表【order(拼车订单表)】的数据库操作Service
* @createDate 2023-04-25 11:27:12
*/
public interface OrderService extends IService<Order> {

    R startPay(Long orderId, LoginAccount loginAccount);

    String checkSignAndConfirm(Map<String, String> notifyParams);

    boolean transfer(Order order);

    R updatePay(Long orderId, LoginAccount loginAccount);
}
