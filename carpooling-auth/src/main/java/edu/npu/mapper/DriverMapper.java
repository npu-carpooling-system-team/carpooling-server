package edu.npu.mapper;

import edu.npu.entity.Driver;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【driver(司机表)】的数据库操作Mapper
* @createDate 2023-04-15 20:48:34
* @Entity edu.npu.entity.Driver
*/
@Mapper
public interface DriverMapper extends BaseMapper<Driver> {

}




