package edu.npu.util;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.common.UnCachedOperationEnum;
import edu.npu.exception.CarpoolingException;
import edu.npu.service.FailCachedCarpoolingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static edu.npu.common.RedisConstants.LOCK_CARPOOLING_KEY;

/**
 * @author : [wangminan]
 * @description : [Redis查询工具类]
 */
@Slf4j
@Component
public class RedisClient {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private FailCachedCarpoolingService failCachedCarpoolingService;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors() * 2);

    @Resource
    private ObjectMapper objectMapper;

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    public <ID> void setWithLogicalExpire(String keyPrefix, ID id, Object value, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 设置逻辑过期
        RedisData redisData = new RedisData(
                LocalDateTime.now().plusSeconds(unit.toSeconds(time)), value);
        // 写入Redis
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(redisData));
        } catch (JsonProcessingException e) {
            log.error("RedisClient setWithLogicalExpire error", e);
            failCachedCarpoolingService
                    .saveCachedFileLogToDb(id, UnCachedOperationEnum.SET);
        }
    }

    /**
     * 使用逻辑过期来解决缓存雪崩 同时由于缓存预热 所以解决了缓存击穿
     * 但需要缓存预热
     *
     * @param keyPrefix 前缀
     * @param id id
     * @param type 类型
     * @param dbFallback 数据库回调
     * @param time 逻辑过期时间
     * @param unit 逻辑过期时间单位
     * @return R
     * @param <R> R
     * @param <ID> ID
     */
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (!StringUtils.hasText(json)) {
            // 3.不存在，直接返回
            return null;
        }
        // 4.命中，需要先把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.data(), type);
        LocalDateTime expireTime = redisData.expireTime();
        // 5.判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())) {
            // 5.1.未过期，直接返回店铺信息
            return r;
        }
        // 5.2.已过期，需要缓存重建
        // 6.缓存重建
        // 6.1.获取互斥锁
        String lockKey = LOCK_CARPOOLING_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 6.2.判断是否获取锁成功
        if (isLock){
            // 6.3.成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    R newR = dbFallback.apply(id);
                    // 重建缓存
                    this.setWithLogicalExpire(keyPrefix, id, newR, time, unit);
                } catch (Exception e) {
                    throw new CarpoolingException(e.getMessage());
                }finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        // 6.4.返回过期的商铺信息
        return r;
    }
}
