package edu.npu.feignClient;

import edu.npu.entity.Driver;
import edu.npu.entity.LoginAccount;
import edu.npu.feignClient.fallback.DriverServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "user-api",
        path = "/api/user",
        fallbackFactory = DriverServiceClientFallbackFactory.class)
public interface DriverServiceClient {
    @PostMapping("/getDriver")
    Driver getDriverWithLoginAccount(@RequestBody LoginAccount loginAccount);
}
