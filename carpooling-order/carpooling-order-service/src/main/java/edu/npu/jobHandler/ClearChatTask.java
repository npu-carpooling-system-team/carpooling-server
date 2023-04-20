package edu.npu.jobHandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : [执行清理聊天记录的分片广播定时任务]
 */
@Slf4j
@Component
public class ClearChatTask {

    //任务调度入口
    @XxlJob("ClearChatJobHandler")
    public void clearChatJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
    }
}
