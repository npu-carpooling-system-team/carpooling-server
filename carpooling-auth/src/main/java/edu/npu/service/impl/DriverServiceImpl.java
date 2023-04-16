package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Driver;
import edu.npu.mapper.DriverMapper;
import edu.npu.service.DriverService;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【driver(司机表)】的数据库操作Service实现
* @createDate 2023-04-16 11:59:56
*/
@Service
public class DriverServiceImpl extends ServiceImpl<DriverMapper, Driver>
    implements DriverService{

}




