package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【user(用户表,用于记录用户的详细信息)】的数据库操作Mapper
* @createDate 2023-04-17 11:23:58
* @Entity edu.npu.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




