package edu.npu.feignClient;

import edu.npu.entity.Carpooling;
import edu.npu.feignClient.fallback.CarpoolingServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author wangminan
 * @description 远程调用carpooling-api服务
 */
@FeignClient(value = "carpooling-api",
        path = "/carpooling/remote",
        fallbackFactory = CarpoolingServiceClientFallbackFactory.class)
public interface CarpoolingServiceClient {
    @GetMapping("/{id}")
    Carpooling getCarpoolingById(@PathVariable("id") Long id);

    @GetMapping("/getList/{driverId}")
    List<Carpooling> getCarpoolingListByDriverId(@PathVariable("driverId") Long driverId);
}
