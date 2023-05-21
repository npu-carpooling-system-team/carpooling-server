package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.Driver;
import edu.npu.vo.R;

/**
* @author wangminan
* @description 针对表【driver(司机表)】的数据库操作Service
* @createDate 2023-04-17 16:24:23
*/
public interface DriverService extends IService<Driver> {

    R genDriverSimpleList();
}
