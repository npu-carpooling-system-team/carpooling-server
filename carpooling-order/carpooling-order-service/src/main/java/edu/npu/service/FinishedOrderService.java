package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.Order;
import edu.npu.vo.R;

public interface FinishedOrderService extends IService<Order> {
    R rateDriver(Long orderId, Integer score);
}
