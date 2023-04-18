package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.AddCarpoolingDto;
import edu.npu.entity.Carpooling;
import edu.npu.entity.LoginAccount;
import edu.npu.vo.R;
import org.springframework.transaction.annotation.Transactional;

/**
* @author wangminan
* @description 针对表【carpooling(拼车行程表)】的数据库操作Service
* @createDate 2023-04-17 19:50:53
*/
public interface CarpoolingService extends IService<Carpooling> {

    @Transactional(rollbackFor = Exception.class)
    R addCarpooling(AddCarpoolingDto addCarpoolingDto, LoginAccount loginAccount);

    @Transactional(rollbackFor = Exception.class)
    boolean saveCarpoolingToEs(Carpooling carpooling);

    @Transactional(rollbackFor = Exception.class)
    R updateCarpooling(AddCarpoolingDto addCarpoolingDto, LoginAccount loginAccount);

    @Transactional(rollbackFor = Exception.class)
    boolean updateCarpoolingToEs(Carpooling carpooling);
}
