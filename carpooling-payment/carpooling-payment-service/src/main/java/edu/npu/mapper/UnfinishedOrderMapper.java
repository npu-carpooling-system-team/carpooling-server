package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.UnfinishedOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author wangminan
* @description 针对表【unfinished_order】的数据库操作Mapper
* @createDate 2023-04-25 11:27:12
* @Entity edu.npu.entity.UnfinishedOrder
*/
@Mapper
public interface UnfinishedOrderMapper extends BaseMapper<UnfinishedOrder> {

    @Select("SELECT * FROM unfinished_order WHERE id % #{shardTotal} = #{shardIndex} LIMIT #{count}")
    List<UnfinishedOrder> selectListByShardIndex(@Param("shardIndex") int shardIndex,
                                                 @Param("shardTotal") int shardTotal,
                                                 @Param("int count") int count);
}




