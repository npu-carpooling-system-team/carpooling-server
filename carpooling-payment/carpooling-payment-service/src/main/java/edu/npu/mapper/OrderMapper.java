package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* @author wangminan
* @description 针对表【order(拼车订单表)】的数据库操作Mapper
* @createDate 2023-04-25 11:27:12
* @Entity edu.npu.entity.Order
*/
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Select("SELECT * FROM `order` WHERE id = #{orderId}")
    Order getById(@Param("orderId") long orderId);

    @Update("UPDATE `order` " +
            "SET status = #{order.status} " +
            "WHERE id = #{order.id}")
    int updateOrderStatus(@Param("order") Order order);
}




