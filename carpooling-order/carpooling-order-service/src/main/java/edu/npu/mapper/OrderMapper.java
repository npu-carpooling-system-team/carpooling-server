package edu.npu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.npu.entity.Order;
import edu.npu.vo.PrizeVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author wangminan
 * @description 针对表【order(拼车订单表)】的数据库操作Mapper
 * @createDate 2023-04-20 14:21:40
 * @Entity edu.npu.entity.Order
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 重写了service的save方法
     * 我不知道为什么前端直传字符串形式的date到后端 后端发给mysql就可以
     * 代码里直接饮用new Date()就要出问题 所以自己写SQL算了
     *
     * @param carpoolingId 拼车id
     * @param passengerId 乘客id
     * @param status 订单状态
     * @return 是否保存成功
     */
    @Insert("INSERT INTO `order` (carpooling_id, passenger_id, status, create_time) " +
            "VALUES (#{carpoolingId}, #{passengerId}, #{status}, now())")
    boolean save(@Param("carpoolingId") long carpoolingId,
                 @Param("passengerId") long passengerId,
                 @Param("status") int status);

    List<PrizeVo> selectPrizeList(@Param("begin")Date begin,
                                  @Param("end") Date end);
}




