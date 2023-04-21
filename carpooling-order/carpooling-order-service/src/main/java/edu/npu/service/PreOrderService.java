package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.PassOrderDto;
import edu.npu.entity.LoginAccount;
import edu.npu.entity.Order;
import edu.npu.vo.R;

/**
* 预定拼车前业务处理
* @author wangminan
* @description 针对表【order(拼车订单表)】的数据库操作Service
* @createDate 2023-04-20 14:21:40
*/
public interface PreOrderService extends IService<Order> {

    R passengerApply(Long carpoolingId, LoginAccount loginAccount);

    R driverGetConfirmList(Long carpoolingId, LoginAccount loginAccount);

    R driverConfirm(PassOrderDto passOrderDto, LoginAccount loginAccount);
}
