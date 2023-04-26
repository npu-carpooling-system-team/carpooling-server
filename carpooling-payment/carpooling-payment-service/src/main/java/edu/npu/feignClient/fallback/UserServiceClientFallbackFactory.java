package edu.npu.feignClient.fallback;

import edu.npu.entity.Driver;
import edu.npu.entity.User;
import edu.npu.feignClient.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [远程调用user-api服务失败回调]
 */
@Slf4j
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    public static final String REMOTE_CALL_FAILED = "远程调用user-api服务失败,原因:{}";

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public User getUserByAccountUsername(String username) {
                log.error(REMOTE_CALL_FAILED, cause.getMessage());
                return null;
            }

            @Override
            public User getUserById(Long id) {
                log.error(REMOTE_CALL_FAILED, cause.getMessage());
                return null;
            }

            @Override
            public Driver getDriverByAccountUsername(String username) {
                log.error(REMOTE_CALL_FAILED, cause.getMessage());
                return null;
            }

            @Override
            public List<Driver> getDriverList() {
                log.error(REMOTE_CALL_FAILED, cause.getMessage());
                return null;
            }

            @Override
            public boolean updateDriver(Driver driver) {
                log.error(REMOTE_CALL_FAILED, cause.getMessage());
                return false;
            }

            @Override
            public boolean banAccount(User user) {
                log.error(REMOTE_CALL_FAILED, cause.getMessage());
                return false;
            }
        };
    }
}
