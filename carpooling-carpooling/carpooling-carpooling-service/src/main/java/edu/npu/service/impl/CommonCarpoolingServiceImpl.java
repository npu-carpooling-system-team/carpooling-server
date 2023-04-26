package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.entity.Carpooling;
import edu.npu.mapper.CarpoolingMapper;
import edu.npu.service.CommonCarpoolingService;
import edu.npu.util.RedisClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static edu.npu.common.RedisConstants.CACHE_CARPOOLING_KEY;
import static edu.npu.common.RedisConstants.CACHE_CARPOOLING_TTL;

/**
 * @author : [wangminan]
 * @description : [通用拼车行程服务实现类]
 */
@Service
public class CommonCarpoolingServiceImpl extends ServiceImpl<CarpoolingMapper, Carpooling>
        implements CommonCarpoolingService {

    @Resource
    private RedisClient redisClient;

    @Override
    public Carpooling getFromCache(Long id) {
        return redisClient.queryWithLogicalExpire(
                CACHE_CARPOOLING_KEY, id, Carpooling.class, this::getById, CACHE_CARPOOLING_TTL, TimeUnit.MINUTES
        );
    }
}
