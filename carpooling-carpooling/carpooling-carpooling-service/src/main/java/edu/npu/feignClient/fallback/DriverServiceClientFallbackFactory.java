package edu.npu.feignClient.fallback;

import edu.npu.entity.Driver;
import edu.npu.feignClient.DriverServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author : [wangminan]
 * @description : [feignClient熔断器]
 */
@Slf4j
public class DriverServiceClientFallbackFactory implements FallbackFactory<DriverServiceClient> {
    @Override
    public DriverServiceClient create(Throwable cause) {
        return loginAccount -> {
            log.error("远程调用用户服务熔断异常：{}", cause.getMessage());
            return new Driver();
        };
    }
}
