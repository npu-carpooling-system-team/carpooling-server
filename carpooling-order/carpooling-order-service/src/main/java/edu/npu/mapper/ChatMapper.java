package edu.npu.mapper;

import edu.npu.entity.Chat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【chat(用户与司机交流表)】的数据库操作Mapper
* @createDate 2023-04-20 14:21:40
* @Entity edu.npu.entity.Chat
*/
@Mapper
public interface ChatMapper extends BaseMapper<Chat> {

}




