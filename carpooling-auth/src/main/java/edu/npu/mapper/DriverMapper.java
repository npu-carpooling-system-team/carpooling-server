package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Driver;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【driver(司机表)】的数据库操作Mapper
* @createDate 2023-04-16 11:59:56
* @Entity edu.npu.entity.Driver
*/
@Mapper
public interface DriverMapper extends BaseMapper<Driver> {

}




