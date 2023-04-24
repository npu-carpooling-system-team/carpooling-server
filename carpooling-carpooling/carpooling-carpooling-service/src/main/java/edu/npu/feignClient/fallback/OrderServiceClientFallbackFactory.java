package edu.npu.feignClient.fallback;

import edu.npu.feignClient.OrderServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author : [wangminan]
 * @description : [远程发信Client FallbackFactory]
 */
@Slf4j
public class OrderServiceClientFallbackFactory implements FallbackFactory<OrderServiceClient> {
    @Override
    public OrderServiceClient create(Throwable throwable) {
        return new OrderServiceClient() {
            @Override
            public void sendNoticeMailToUser(Long carpoolingId, String subject, String content) {
                log.error("远程调用order-api服务失败", throwable);
            }

            @Override
            public void forceCloseOrderByCarpoolingId(Long carpoolingId) {
                log.error("远程调用order-api服务失败", throwable);
            }

            @Override
            public boolean checkHasPassenger(Long carpoolingId) {
                log.error("远程调用order-api服务失败", throwable);
                return false;
            }
        };
    }
}
