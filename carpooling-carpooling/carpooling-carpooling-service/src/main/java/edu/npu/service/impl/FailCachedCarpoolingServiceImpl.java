package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.UnCachedOperationEnum;
import edu.npu.entity.Carpooling;
import edu.npu.entity.FailCachedCarpooling;
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

    @Override
    public boolean syncFailCachedCarpooling(int shardIndex, int shardTotal, int i) {
        boolean flag = true;
        List<FailCachedCarpooling> failCachedCarpoolingList =
                failCachedCarpoolingMapper.selectListByShardIndex(shardIndex, shardTotal, i);
        // 将数据根据operationType同步到Redis与ES中
        for (FailCachedCarpooling failCachedCarpooling : failCachedCarpoolingList) {
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
                log.error("FailCachedCarpoolingServiceImpl.syncFailCachedCarpooling: " +
                                "operationType is not valid, operationType = {}",
                        failCachedCarpooling.getOperationType());
                flag = false;
            }
        }
        return flag;
    }

    @Override
    public <ID> void saveCachedFileLogToDb(ID carpoolingId,
                                           UnCachedOperationEnum operationEnum) {
        FailCachedCarpooling failCachedCarpooling =
                new FailCachedCarpooling((long) carpoolingId, operationEnum.getValue());
        failCachedCarpoolingMapper.insert(failCachedCarpooling);
    }
}




