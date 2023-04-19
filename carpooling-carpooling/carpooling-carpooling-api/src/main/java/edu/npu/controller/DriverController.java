package edu.npu.controller;

import edu.npu.dto.AddCarpoolingDto;
import edu.npu.dto.EditCarpoolingDto;
import edu.npu.dto.PageQueryDto;
import edu.npu.entity.LoginAccount;
import edu.npu.exception.CarpoolingError;
import edu.npu.exception.CarpoolingException;
import edu.npu.service.DriverCarpoolingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [司机CRUD拼车形成的Controller]
 */
@RestController
@Slf4j
@RequestMapping("/driver")
public class DriverController {

    @Resource
    private DriverCarpoolingService driverCarpoolingService;

    @GetMapping("/carpooling")
    public R getCarpooling(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(required = false) String query,
                           @RequestParam(required = false) String departureTime,
                           @RequestParam(required = false) String arriveTime,
                           @AuthenticationPrincipal LoginAccount loginAccount) {
        PageQueryDto pageQueryDto = new PageQueryDto();
        pageQueryDto.setPageNum(pageNum);
        pageQueryDto.setPageSize(pageSize);
        if (query != null) {
            pageQueryDto.setQuery(query);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (departureTime != null) {
            // 传入的时间格式为yyyy-MM-dd HH:mm 用SimpleDateFormat转换为Date
            try {
                Date date = format.parse(departureTime);
                pageQueryDto.setDepartureTime(date);
            } catch (Exception e) {
                log.error("departureTime时间转换错误:{}", departureTime);
                CarpoolingException.cast(CarpoolingError.PARAMS_ERROR, "时间转换错误");
            }
        }
        if (arriveTime != null) {
            try {
                Date date = format.parse(arriveTime);
                pageQueryDto.setArriveTime(date);
            } catch (Exception e) {
                log.error("arriveTime时间转换错误:{}", arriveTime);
                CarpoolingException.cast(CarpoolingError.PARAMS_ERROR, "时间转换错误");
            }
        }
        log.info("pageQueryDto: {}", pageQueryDto);
        return driverCarpoolingService.getCarpooling(pageQueryDto, loginAccount);
    }

    @PostMapping("/carpooling")
    public R addCarpooling(@RequestBody @Validated AddCarpoolingDto addCarpoolingDto,
                           @AuthenticationPrincipal LoginAccount loginAccount) {
        return driverCarpoolingService.addCarpooling(addCarpoolingDto, loginAccount);
    }

    @PutMapping("/carpooling/{id}")
    public R updateCarpooling(@PathVariable("id") Long id,
                              @RequestBody @Validated EditCarpoolingDto editCarpoolingDto,
                              @AuthenticationPrincipal LoginAccount loginAccount) {
        return driverCarpoolingService.updateCarpooling(id, editCarpoolingDto, loginAccount);
    }

    @DeleteMapping("/carpooling/{id}")
    public R deleteCarpooling(@PathVariable("id") Long id,
                              @AuthenticationPrincipal LoginAccount loginAccount) {
        return driverCarpoolingService.deleteCarpooling(id, loginAccount);
    }
}
