package edu.npu.feignClient.fallback;

import edu.npu.entity.Carpooling;
import edu.npu.feignClient.CarpoolingServiceClient;
import edu.npu.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : [wangminan]
 * @description : [carpooling-api服务远程调用的回调工厂类]
 */
@Slf4j
@Component
public class CarpoolingServiceClientFallbackFactory
        implements FallbackFactory<CarpoolingServiceClient> {
    @Override
    public CarpoolingServiceClient create(Throwable cause) {
        return new CarpoolingServiceClient() {
            @Override
            public Carpooling getCarpoolingById(Long id) {
                log.error("远程调用carpooling-api服务失败,原因:{}", cause.getMessage());
                return null;
            }

            @Override
            public List<Carpooling> getCarpoolingListByDriverId(Long driverId) {
                log.error("远程调用carpooling-api服务失败,原因:{}", cause.getMessage());
                return new ArrayList<>();
            }

            @Override
            public R updateCarpooling(Carpooling carpooling) {
                return new R();
            }
        };
    }
}
