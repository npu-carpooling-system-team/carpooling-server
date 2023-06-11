package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.Carpooling;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

public interface CommonCarpoolingService extends IService<Carpooling> {
    Carpooling getFromCache(Long id);

    @Transactional(rollbackFor = Exception.class)
    R updateCarpooling(Carpooling carpooling);
}
