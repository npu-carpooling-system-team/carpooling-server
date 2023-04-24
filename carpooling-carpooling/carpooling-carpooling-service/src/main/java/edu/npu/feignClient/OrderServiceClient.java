package edu.npu.feignClient;

import edu.npu.feignClient.fallback.DriverServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "order-api",
        path = "/order",
        fallbackFactory = DriverServiceClientFallbackFactory.class)
public interface OrderServiceClient {
    @GetMapping("/mail")
    void sendNoticeMailToUser(
            @RequestParam(value = "carpoolingId") Long carpoolingId,
            @RequestParam(value = "subject") String subject,
            @RequestParam(value = "content") String content
    );

    @PutMapping("/{carpoolingId}")
    void forceCloseOrderByCarpoolingId(
            @PathVariable(value = "carpoolingId") Long carpoolingId);

    @GetMapping
    boolean checkHasPassenger(
            @RequestParam(value = "carpoolingId") Long carpoolingId
    );
}
