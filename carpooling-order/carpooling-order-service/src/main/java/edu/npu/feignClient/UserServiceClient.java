package edu.npu.feignClient;

import edu.npu.entity.Driver;
import edu.npu.entity.User;
import edu.npu.feignClient.fallback.UserServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程调用user-api服务]
 */
@FeignClient(value = "user-api",
        path = "/user/remote",
        fallbackFactory = UserServiceClientFallbackFactory.class)
public interface UserServiceClient {

    @GetMapping("/getUserByUsername")
    User getUserByAccountUsername(
            @RequestParam(value = "username") String username
    );

    @GetMapping("/getUserById")
    User getUserById(
            // feign的要求是一定要写value 不能省略
            // 好吧 不要用path来传参数 会变得不幸
            @RequestParam(value = "id") Long id
    );

    @GetMapping("/getDriver")
    Driver getDriverByAccountUsername(
            @RequestParam(value = "username") String username
    );

    @GetMapping("/getDriverList")
    List<Driver> getDriverList();

    @PutMapping("/updateDriver")
    boolean updateDriver(@RequestBody Driver driver);
}
