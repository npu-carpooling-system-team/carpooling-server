package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Chat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author wangminan
* @description 针对表【chat(用户与司机交流表)】的数据库操作Mapper
* @createDate 2023-04-20 14:21:40
* @Entity edu.npu.entity.Chat
*/
@Mapper
public interface ChatMapper extends BaseMapper<Chat> {

    @Select("SELECT * FROM chat WHERE id % #{shardTotal} = #{shardIndex} limit #{count}")
    List<Chat> selectListByShardIndex(@Param("shardTotal")int shardIndex,
                                      @Param("shardIndex")int shardTotal,
                                      @Param("count") int count);
}




