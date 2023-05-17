package edu.npu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.entity.Carpooling;
import edu.npu.service.CommonCarpoolingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author : [wangminan]
 * @description : [用于远程调用获取信息的接口]
 */
@RestController
public class CarpoolingController {

    @Resource
    private CommonCarpoolingService carpoolingService;

    /**
     * 远程调用使用接口 用于通过id获取拼车行程信息
     * @param id 拼车行程ID
     * @return 返回拼车行程信息
     */
    @GetMapping("/remote/{id}")
    public Carpooling getCarpoolingById(@PathVariable(value = "id") Long id) {
        return carpoolingService.getFromCache(id);
    }

    @GetMapping("/frontend/{id}")
    public R frontendGetCarpoolingById(@PathVariable Long id) {
        return R.ok().put("carpooling",carpoolingService.getFromCache(id));
    }

    @GetMapping("/remote/getList/{driverId}")
    public List<Carpooling> getCarpoolingListByDriverId(
            @PathVariable(value = "driverId") Long driverId){
        return carpoolingService.list(
                new LambdaQueryWrapper<Carpooling>()
                        .eq(Carpooling::getDriverId, driverId)
        );
    }
}
