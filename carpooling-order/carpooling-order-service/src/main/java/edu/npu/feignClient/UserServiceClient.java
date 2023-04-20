package edu.npu.feignClient;

import edu.npu.entity.User;
import edu.npu.feignClient.fallback.UserServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author : [wangminan]
 * @description : [远程调用user-api服务]
 */
@FeignClient(value = "user-api",
        path = "/user",
        fallbackFactory = UserServiceClientFallbackFactory.class)
public interface UserServiceClient {

    @GetMapping("/getUser")
    User getUserWithAccountUsername(
            @RequestParam("username") String username
    );

    @GetMapping("/getUser/{id}")
    User getUserWithId(
            @PathVariable("id") Long id
    );
}
