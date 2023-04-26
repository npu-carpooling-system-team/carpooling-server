package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.Carpooling;

public interface CommonCarpoolingService extends IService<Carpooling> {
    Carpooling getFromCache(Long id);
}
