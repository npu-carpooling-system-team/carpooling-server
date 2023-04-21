package edu.npu.feignClient.fallback;

import edu.npu.entity.Carpooling;
import edu.npu.feignClient.CarpoolingServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author : [wangminan]
 * @description : [carpooling-api服务远程调用的回调工厂类]
 */
@Slf4j
public class CarpoolingServiceClientFallbackFactory
        implements FallbackFactory<CarpoolingServiceClient> {
    @Override
    public CarpoolingServiceClient create(Throwable cause) {
        return new CarpoolingServiceClient() {
            @Override
            public Carpooling getCarpoolingById(Long id) {
                log.error("feignClient熔断器触发，原因：{}", cause.getMessage());
                return new Carpooling();
            }
        };
    }
}
