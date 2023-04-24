package edu.npu.service;

import edu.npu.vo.R;

import java.util.Date;

public interface AdminService{

    /**
     * 生成订单列表到OSS
     * @param begin 开始时间
     * @param end 结束时间
     * @param driverId 司机ID
     * @return R
     */
    R genOrderList(Date begin, Date end, Long driverId);

    /**
     * 生成奖励司机列表到OSS
     * @param begin 开始时间
     * @param end 结束时间
     * @return R
     */
    R genPrizeList(Date begin, Date end);
}
