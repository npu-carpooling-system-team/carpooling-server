package edu.npu.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import edu.npu.service.UnfinishedOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : [定时校验与关单的分片广播定时任务]
 */
@Slf4j
@Component
public class ClearUnfinishedOrderTask {

    @Resource
    private UnfinishedOrderService unfinishedOrderService;

    //任务调度入口
    @XxlJob("ClearUnfinishedOrderHandler")
    public void clearUnfinishedOrderHandler() {
        log.debug("执行清理聊天记录的分片广播定时任务");
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        // 通过chatService删除发送一周以上且已读的聊天记录
        boolean deleteSuccess = unfinishedOrderService
                .closeOrder(shardIndex, shardTotal, 100);
        if (!deleteSuccess){
            log.error("定时关单失败,请检查后端服务是否正常");
        }
    }
}
