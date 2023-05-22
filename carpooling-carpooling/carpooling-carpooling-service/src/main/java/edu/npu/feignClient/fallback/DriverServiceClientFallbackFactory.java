package edu.npu.feignClient.fallback;

import edu.npu.entity.Driver;
import edu.npu.feignClient.DriverServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [feignClient熔断器]
 */
@Slf4j
@Component
public class DriverServiceClientFallbackFactory implements FallbackFactory<DriverServiceClient> {

    @Override
    public DriverServiceClient create(Throwable cause) {
        // 有多个方法的时候就不能Lambda表达式了 得用匿名内部类一个一个处理
        return new DriverServiceClient() {
            @Override
            public Driver getDriverByAccountUsername(String username) {
                log.error("feignClient熔断器触发，原因:{}", cause.getMessage());
                return new Driver();
            }

            @Override
            public List<Driver> getDriverList() {
                log.error("feignClient熔断器触发，原因:{}", cause.getMessage());
                return new ArrayList<>();
            }
        };
    }
}
