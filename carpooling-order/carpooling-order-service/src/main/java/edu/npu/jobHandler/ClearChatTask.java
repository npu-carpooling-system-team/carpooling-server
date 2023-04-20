package edu.npu.jobHandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import edu.npu.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : [执行清理聊天记录的分片广播定时任务]
 */
@Slf4j
@Component
public class ClearChatTask {

    @Resource
    private ChatService chatService;

    //任务调度入口
    @XxlJob("ClearChatJobHandler")
    public void clearChatJobHandler() {
        log.debug("执行清理聊天记录的分片广播定时任务");
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        // 通过chatService删除发送一周以上且已读的聊天记录
        boolean deleteSuccess = chatService.deleteChatRecord(shardIndex, shardTotal, 100);
        if (!deleteSuccess){
            log.error("删除聊天记录失败");
        }
    }
}
