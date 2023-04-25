package edu.npu.mapper;

import edu.npu.entity.UnfinishedOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【unfinished_order】的数据库操作Mapper
* @createDate 2023-04-25 11:27:12
* @Entity edu.npu.entity.UnfinishedOrder
*/
@Mapper
public interface UnfinishedOrderMapper extends BaseMapper<UnfinishedOrder> {

}




