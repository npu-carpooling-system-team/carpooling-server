package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.UnfinishedOrder;
import edu.npu.mapper.OrderMapper;
import edu.npu.mapper.UnfinishedOrderMapper;
import edu.npu.service.UnfinishedOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author wangminan
* @description 针对表【unfinished_order】的数据库操作Service实现
* @createDate 2023-04-25 11:27:12
*/
@Service
public class UnfinishedOrderServiceImpl extends ServiceImpl<UnfinishedOrderMapper, UnfinishedOrder>
    implements UnfinishedOrderService{

    @Resource
    private UnfinishedOrderMapper unfinishedOrderMapper;

    @Resource
    private OrderMapper orderMapper;

    @Override
    public boolean closeOrder(int shardIndex, int shardTotal, int count) {
        // 根据给出的分片索引和分片总数,计算出需要删除的聊天记录的id范围
        // 例如:分片索引为0,分片总数为2,则需要删除id为1,3,5,7,9...的聊天记录
        List<UnfinishedOrder> unfinishedOrders =
                unfinishedOrderMapper.selectListByShardIndex(shardIndex, shardTotal, count);
        // 查询对应的Order的update_time,如果超过1天,则关闭订单
        return false;
    }
}




