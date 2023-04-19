package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.entity.Carpooling;
import edu.npu.entity.Driver;
import edu.npu.feignClient.DriverServiceClient;
import edu.npu.mapper.CarpoolingMapper;
import edu.npu.service.AdminCarpoolingService;
import edu.npu.vo.R;
import edu.npu.vo.SimpleDriverVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Service
public class AdminCarpoolingServiceImpl extends ServiceImpl<CarpoolingMapper, Carpooling>
        implements AdminCarpoolingService {

    @Resource
    private DriverServiceClient driverServiceClient;

    /**
     * 因此提供此接口用于检索车主列表。仅提供id-姓名对应，不需要输入参数
     *
     * @return R
     */
    @Override
    public R getDriverList() {
        List<Driver> driverList = driverServiceClient.getDriverList();
        List<SimpleDriverVo> simpleDrivers = new ArrayList<>();
        driverList
                .stream()
                .map(driver ->
                        new SimpleDriverVo(
                                driver.getDriverId(), driver.getDriversName()))
                .forEach(simpleDrivers::add);
        Map<String, Object> result = new HashMap<>();
        result.put("drivers", simpleDrivers);
        return R.ok(result);
    }
}
