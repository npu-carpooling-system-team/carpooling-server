package edu.npu.controller;

import edu.npu.dto.PageQueryDto;
import edu.npu.exception.CarpoolingError;
import edu.npu.exception.CarpoolingException;
import edu.npu.service.PassengerCarpoolingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;

/**
 * @author : [wangminan]
 * @description : [乘客查看拼车行程的Controller]
 */
@RestController
@Slf4j
@RequestMapping("/passenger")
public class PassengerController {

    @Resource
    private PassengerCarpoolingService passengerCarpoolingService;

    @GetMapping("/carpooling")
    public R getCarpoolingList(@RequestParam String pageSize,
                               @RequestParam String pageNum,
                               @RequestParam(required = false) String query,
                               @RequestParam(required = false) String departureTime,
                               @RequestParam(required = false) String arriveTime) {
        PageQueryDto pageQueryDto = new PageQueryDto();
        pageQueryDto.setPageNum(Integer.parseInt(pageNum));
        pageQueryDto.setPageSize(Integer.parseInt(pageSize));
        if (query != null) {
            pageQueryDto.setQuery(query);
        }
        if (departureTime != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                pageQueryDto.setDepartureTime(simpleDateFormat.parse(departureTime));
            } catch (Exception e) {
                log.error("时间转换错误, departureTime: {}", departureTime);
                CarpoolingException.cast(CarpoolingError.PARAMS_ERROR, "时间转换错误");
            }
        }
        if (arriveTime != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                pageQueryDto.setArriveTime(simpleDateFormat.parse(arriveTime));
            } catch (Exception e) {
                log.error("时间转换错误, arriveTime: {}", arriveTime);
                CarpoolingException.cast(CarpoolingError.PARAMS_ERROR, "时间转换错误");
            }
        }
        return passengerCarpoolingService.getCarpoolingList(pageQueryDto);
    }
}
