package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.Carpooling;
import edu.npu.vo.R;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
public interface AdminCarpoolingService extends IService<Carpooling> {
    R getDriverList();
}
