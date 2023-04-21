package edu.npu.feignClient;

import edu.npu.entity.Driver;
import edu.npu.feignClient.fallback.DriverServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author wangminan
 * @description 远程调用user模块获取driver信息的feignClient
 */
@FeignClient(value = "user-api",
        path = "/user",
        fallbackFactory = DriverServiceClientFallbackFactory.class)
public interface DriverServiceClient {
    @GetMapping("/getDriver")
    Driver getDriverWithAccountUsername(
            @RequestParam("username") String username
    );

    @GetMapping("/getDriverList")
    List<Driver> getDriverList();
}
