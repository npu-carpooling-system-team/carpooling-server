package edu.npu.controller;

import edu.npu.entity.Carpooling;
import edu.npu.mapper.CarpoolingMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [wangminan]
 * @description : [用于远程调用获取信息的接口]
 */
@RestController
public class CarpoolingController {

    @Resource
    private CarpoolingMapper carpoolingMapper;

    /**
     * 远程调用接口 用于通过id获取拼车行程信息
     * @param id 拼车行程ID
     * @return 返回拼车行程信息
     */
    @GetMapping("/{id}")
    public Carpooling getCarpoolingById(@PathVariable Long id) {
        return carpoolingMapper.selectById(id);
    }
}
