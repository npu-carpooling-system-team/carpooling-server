package edu.npu.controller;

import edu.npu.exception.CarpoolingError;
import edu.npu.exception.CarpoolingException;
import edu.npu.service.AdminGeneralService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [管理员处理拼车情况的业务类]
 */
@RestController("/admin")
@Slf4j
public class AdminGeneralController {

    @Resource
    private AdminGeneralService adminGeneralService;

    private static final Long FINAL_DATE = 4102415999000L;

    private static Date begin, end;

    @GetMapping("/carpooling/list")
    public R getCarpoolingListForDriver(
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "driverId", required = false) Long driverId) {
        formatDate(beginTime, endTime);
        return adminGeneralService.genOrderList(begin, end, driverId);
    }

    @GetMapping("/prize/list")
    public R genPrizeList(
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        formatDate(beginTime, endTime);
        return adminGeneralService.genPrizeList(begin, end);
    }

    private void formatDate(String beginTime, String endTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (StringUtils.hasText(beginTime)){
                begin = sdf.parse(beginTime);
            } else {
                // 给begin一个很早很早的时间当初始值 1970-01-01
                begin = new Date(0);
            }
            if (StringUtils.hasText(endTime)){
                end = sdf.parse(endTime);
            } else {
                // 给end一个很晚很晚的时间当初始值 2099-12-31
                end = new Date(FINAL_DATE);
            }
        } catch (ParseException e) {
            log.error("日期转换错误");

            CarpoolingException.cast(
                    CarpoolingError.PARAMS_ERROR, "您给出的日期无法被正确转换");
        }
    }
}
