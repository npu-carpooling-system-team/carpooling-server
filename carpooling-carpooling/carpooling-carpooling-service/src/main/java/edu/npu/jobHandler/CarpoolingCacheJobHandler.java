package edu.npu.jobHandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import edu.npu.service.FailCachedCarpoolingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : [wangminan]
 * @description : 用于校验MySQL与ES和Redis之间的同步状态的分片广播定时任务
 */
@Slf4j
@Component
public class CarpoolingCacheJobHandler {

    @Resource
    private FailCachedCarpoolingService failCachedCarpoolingService;

    //任务调度入口
    @XxlJob("CarpoolingCacheHandler")
    public void carpoolingCacheHandler() {
        log.info("XXL>>>>>执行校验MySQL与ES和Redis之间的同步状态的分片广播定时任务");
        // 分片参数
        // 分片序号 从0开始// 分片序号 从0开始
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("shardIndex={},shardTotal={}", shardIndex, shardTotal);
        // 将fail_cached_carpooling表中的数据同步到ES和Redis中
        failCachedCarpoolingService
                .syncFailCachedCarpooling(shardIndex, shardTotal, 100);
    }
}
