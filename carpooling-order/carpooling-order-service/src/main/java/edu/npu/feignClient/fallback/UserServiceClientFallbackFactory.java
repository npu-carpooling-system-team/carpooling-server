package edu.npu.feignClient.fallback;

import edu.npu.entity.Driver;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程调用user-api服务失败回调]
 */
@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    private static final String FAILED_CALLAPI_SERVICE_MSG = "远程调用user-api服务失败:{}";

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public User getUserByAccountUsername(String username) {
                log.error(FAILED_CALLAPI_SERVICE_MSG, cause.getMessage());
                return null;
            }

            @Override
            public User getUserById(Long id) {
                log.error(FAILED_CALLAPI_SERVICE_MSG, cause.getMessage());
                return null;
            }

            @Override
            public Driver getDriverByAccountUsername(String username) {
                log.error(FAILED_CALLAPI_SERVICE_MSG, cause.getMessage());
                return null;
            }

            @Override
            public List<Driver> getDriverList() {
                log.error(FAILED_CALLAPI_SERVICE_MSG, cause.getMessage());
                return new ArrayList<>();
            }

            @Override
            public boolean updateDriver(Driver driver) {
                log.error(FAILED_CALLAPI_SERVICE_MSG, cause.getMessage());
                return false;
            }
        };
    }
}
