package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.UnCachedOperationEnum;
import edu.npu.entity.Carpooling;
import edu.npu.entity.FailCachedCarpooling;
import edu.npu.exception.CarpoolingException;
import edu.npu.mapper.CarpoolingMapper;
import edu.npu.mapper.FailCachedCarpoolingMapper;
import edu.npu.service.FailCachedCarpoolingService;
import edu.npu.util.RedisClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.RedisConstants.CACHE_CARPOOLING_KEY;
import static edu.npu.common.RedisConstants.CACHE_CARPOOLING_TTL;

/**
 * @author wangminan
 * @description 针对表【fail_cached_carpooling】的数据库操作Service实现
 * @createDate 2023-05-13 11:42:08
 */
@Service
@Slf4j
public class FailCachedCarpoolingServiceImpl extends ServiceImpl<FailCachedCarpoolingMapper, FailCachedCarpooling>
        implements FailCachedCarpoolingService {

    @Resource
    private FailCachedCarpoolingMapper failCachedCarpoolingMapper;

    @Resource
    private CarpoolingMapper carpoolingMapper;

    @Resource
    @Lazy
    private EsService esService;

    @Resource
    @Lazy
    private RedisClient redisClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final Long DEFAULT_PROCESS_MINUTES = 30L;

    private static final String SYNC_FAILED_CACHED_MSG = "FailCachedCarpoolingServiceImpl.syncFailCachedCarpooling: ";

    @Override
    public void syncFailCachedCarpooling(int shardIndex, int shardTotal, int i) {
        List<FailCachedCarpooling> failCachedCarpoolingList =
                failCachedCarpoolingMapper.selectListByShardIndex(shardIndex, shardTotal, i);
        int size = failCachedCarpoolingList.size();
        log.info("取出:{}条数据,开始执行同步操作",size);

        //创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        // 将数据根据operationType同步到Redis与ES中
        failCachedCarpoolingList.forEach( failCachedCarpooling -> threadPool.execute(() -> {
            if (failCachedCarpooling.getOperationType().equals(
                    UnCachedOperationEnum.INSERT.getValue()
            )) {
                // 插入ES
                Carpooling carpooling = carpoolingMapper
                        .selectById(failCachedCarpooling.getCarpoolingId());
                esService.saveCarpoolingToEs(carpooling);
            } else if (failCachedCarpooling.getOperationType().equals(
                    UnCachedOperationEnum.SET.getValue()
            )) {
                // 插入Redis
                Carpooling carpooling = carpoolingMapper
                        .selectById(failCachedCarpooling.getCarpoolingId());
                redisClient.setWithLogicalExpire(
                        CACHE_CARPOOLING_KEY, carpooling.getId(),
                        carpooling, CACHE_CARPOOLING_TTL, TimeUnit.MINUTES
                );
            } else if (failCachedCarpooling.getOperationType().equals(
                    UnCachedOperationEnum.UPDATE.getValue()
            )) {
                // 更新操作
                Carpooling carpooling = carpoolingMapper
                        .selectById(failCachedCarpooling.getCarpoolingId());
                redisClient.setWithLogicalExpire(
                        CACHE_CARPOOLING_KEY, carpooling.getId(),
                        carpooling, CACHE_CARPOOLING_TTL, TimeUnit.MINUTES
                );
                esService.updateCarpoolingToEs(carpooling);
            } else if (failCachedCarpooling.getOperationType().equals(
                    UnCachedOperationEnum.DELETE.getValue()
            )) {
                // 删除操作
                stringRedisTemplate
                        .delete(CACHE_CARPOOLING_KEY +
                                failCachedCarpooling.getCarpoolingId());
                esService.deleteCarpoolingFromEs(failCachedCarpooling.getCarpoolingId());
            } else {
                log.error(SYNC_FAILED_CACHED_MSG +
                                "operationType is not valid, operationType = {}",
                        failCachedCarpooling.getOperationType());
            }
            countDownLatch.countDown();
        }));

        try {
            boolean await = countDownLatch.await(DEFAULT_PROCESS_MINUTES, TimeUnit.MINUTES);
            if (!await) {
                log.error(SYNC_FAILED_CACHED_MSG +
                        "sync fail, countDownLatch.await timeout");
            } else {
                log.info(SYNC_FAILED_CACHED_MSG +
                        "sync success");
            }
        } catch (InterruptedException e) {
            log.error(SYNC_FAILED_CACHED_MSG +
                    "sync fail, countDownLatch.await interrupted");
            Thread.currentThread().interrupt();
            throw new CarpoolingException(e.getMessage());
        }
    }

    @Override
    public <ID> void saveCachedFileLogToDb(ID carpoolingId,
                                           UnCachedOperationEnum operationEnum) {
        FailCachedCarpooling failCachedCarpooling =
                new FailCachedCarpooling((long) carpoolingId, operationEnum.getValue());
        failCachedCarpoolingMapper.insert(failCachedCarpooling);
    }
}




