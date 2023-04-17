package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Carpooling;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【carpooling(拼车行程表)】的数据库操作Mapper
* @createDate 2023-04-17 19:50:53
* @Entity edu.npu.entity.Carpooling
*/
@Mapper
public interface CarpoolingMapper extends BaseMapper<Carpooling> {

}




