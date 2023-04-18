package edu.npu.feignClient;

import edu.npu.entity.Driver;
import edu.npu.entity.LoginAccount;
import edu.npu.feignClient.fallback.DriverServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user-api",
        path = "/user",
        fallbackFactory = DriverServiceClientFallbackFactory.class)
public interface DriverServiceClient {
    @GetMapping("/getDriver")
    Driver getDriverWithAccountUsername(
            @RequestParam("username") String username
    );
}
