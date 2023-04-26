package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.entity.UnfinishedOrder;

/**
* @author wangminan
* @description 针对表【unfinished_order】的数据库操作Service
* @createDate 2023-04-25 11:27:12
*/
public interface UnfinishedOrderService extends IService<UnfinishedOrder> {

    boolean closeOrder(int shardIndex, int shardTotal, int count);
}
