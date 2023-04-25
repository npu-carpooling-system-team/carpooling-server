package edu.npu.mapper;

import edu.npu.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangminan
* @description 针对表【order(拼车订单表)】的数据库操作Mapper
* @createDate 2023-04-25 11:27:12
* @Entity edu.npu.entity.Order
*/
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}




