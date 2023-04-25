package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.UnfinishedOrder;
import edu.npu.service.UnfinishedOrderService;
import edu.npu.mapper.UnfinishedOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author wangminan
* @description 针对表【unfinished_order】的数据库操作Service实现
* @createDate 2023-04-25 11:27:12
*/
@Service
public class UnfinishedOrderServiceImpl extends ServiceImpl<UnfinishedOrderMapper, UnfinishedOrder>
    implements UnfinishedOrderService{

    @Override
    public boolean closeOrder(int shardIndex, int shardTotal, int i) {
        return false;
    }
}




