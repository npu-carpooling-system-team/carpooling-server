package edu.npu.feignClient.fallback;

import edu.npu.entity.Driver;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程调用user-api服务失败回调]
 */
@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public User getUserByAccountUsername(String username) {
                log.error("远程调用user-api服务失败,原因:{}", cause.getMessage());
                return null;
            }

            @Override
            public User getUserById(Long id) {
                log.error("远程调用user-api服务失败,原因:{}", cause.getMessage());
                return null;
            }

            @Override
            public Driver getDriverByAccountUsername(String username) {
                log.error("远程调用user-api服务失败,原因:{}", cause.getMessage());
                return null;
            }

            @Override
            public List<Driver> getDriverList() {
                log.error("远程调用user-api服务失败,原因:{}", cause.getMessage());
                return null;
            }

            @Override
            public boolean updateDriver(Driver driver) {
                log.error("远程调用user-api服务失败,原因:{}", cause.getMessage());
                return false;
            }
        };
    }
}
