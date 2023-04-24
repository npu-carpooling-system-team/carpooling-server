package edu.npu.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.npu.common.OrderStatusEnum;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.entity.Carpooling;
import edu.npu.entity.Driver;
import edu.npu.entity.Order;
import edu.npu.entity.User;
import edu.npu.exception.CarpoolingError;
import edu.npu.exception.CarpoolingException;
import edu.npu.feignClient.CarpoolingServiceClient;
import edu.npu.feignClient.UserServiceClient;
import edu.npu.mapper.OrderMapper;
import edu.npu.service.AdminService;
import edu.npu.util.OssUtil;
import edu.npu.vo.PrizeVo;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : [wangminan]
 * @description : [管理员服务实现类]
 */
@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Resource
    private OssUtil ossUtil;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private CarpoolingServiceClient carpoolingServiceClient;

    @Override
    public R genOrderList(Date begin, Date end, Long driverId) {
        // 根据条件查询已经完成的订单 生成EXCEL 存储到OSS
        List<Order> orders;
        if (driverId == null) {
            orders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getStatus, OrderStatusEnum.ORDER_NORMAL_CLOSED)
                            .ge(Order::getUpdateTime, begin)
                            .le(Order::getUpdateTime, end)
            );
        } else {
            // 加上司机条件
            // 远程调用user-api获取司机
            User user = userServiceClient.getUserById(driverId);
            Driver driver = userServiceClient.getDriverByAccountUsername(user.getUsername());
            // 先通过司机ID查询出所有的订单ID
            List<Carpooling> carpoolingList =
                    carpoolingServiceClient.getCarpoolingListByDriverId(driver.getId());
            orders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getStatus, OrderStatusEnum.ORDER_NORMAL_CLOSED)
                            .ge(Order::getUpdateTime, begin)
                            .le(Order::getUpdateTime, end)
                            .in(Order::getCarpoolingId, carpoolingList)
            );
        }
        // 生成当天的订单到excel表格中
        try (
                // 创建workbook SXSSFWorkbook默认100行缓存
                Workbook workbook = new SXSSFWorkbook()
        ) {
            // 创建sheet
            Sheet sheet = workbook.createSheet("订单列表");
            // 创建表头
            sheet.createRow(0).createCell(0).setCellValue("订单ID");
            sheet.getRow(0).createCell(1).setCellValue("拼车订单ID");
            sheet.getRow(0).createCell(2).setCellValue("司机_用户ID");
            sheet.getRow(0).createCell(3).setCellValue("司机_手机号码");
            sheet.getRow(0).createCell(4).setCellValue("乘客_用户ID");
            sheet.getRow(0).createCell(5).setCellValue("乘客_手机号码");
            sheet.getRow(0).createCell(6).setCellValue("订单创建时间");
            sheet.getRow(0).createCell(7).setCellValue("订单完成时间");
            sheet.getRow(0).createCell(8).setCellValue("订单金额");
            sheet.getRow(0).createCell(9).setCellValue("订单用户评分");
            // 开始插入数据
            for (Order tmpOrder : orders){
                // 获取当前行
                int lastRowNum = sheet.getLastRowNum();
                // 创建行
                sheet.createRow(lastRowNum + 1);
                // 创建列
                sheet.getRow(lastRowNum + 1).createCell(0).setCellValue(tmpOrder.getId());
                sheet.getRow(lastRowNum + 1).createCell(1).setCellValue(tmpOrder.getCarpoolingId());
                Carpooling carpooling = carpoolingServiceClient.getCarpoolingById(tmpOrder.getCarpoolingId());
                User driver = userServiceClient.getUserById(carpooling.getDriverId());
                User passenger = userServiceClient.getUserById(tmpOrder.getPassengerId());
                sheet.getRow(lastRowNum + 1).createCell(2).setCellValue(driver.getId());
                sheet.getRow(lastRowNum + 1).createCell(3).setCellValue(driver.getUsername());
                sheet.getRow(lastRowNum + 1).createCell(4).setCellValue(passenger.getId());
                sheet.getRow(lastRowNum + 1).createCell(5).setCellValue(passenger.getUsername());
                sheet.getRow(lastRowNum + 1).createCell(6).setCellValue(tmpOrder.getCreateTime());
                sheet.getRow(lastRowNum + 1).createCell(7).setCellValue(tmpOrder.getUpdateTime());
                sheet.getRow(lastRowNum + 1).createCell(8).setCellValue(carpooling.getPrice());
                sheet.getRow(lastRowNum + 1).createCell(9).setCellValue(tmpOrder.getScore());
            }
            File file = File.createTempFile(
                    "订单列表_" +
                            SimpleDateFormat.getDateTimeInstance().format(new Date()),
                    ".xlsx"
            );
            String url = uploadFileToOss(workbook, file);
            return StringUtils.hasText(url) ?
                    R.ok().put("result", url) : R.error("生成订单列表失败");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public R genPrizeList(Date begin, Date end) {
        List<PrizeVo> prizes = orderMapper.selectPrizeList(begin, end);
        // 写入EXCEL
        try(
            // 创建workbook SXSSFWorkbook默认100行缓存
            Workbook workbook = new SXSSFWorkbook()
        ) {
            // 创建Sheet
            Sheet sheet = workbook.createSheet("嘉奖列表");
            // 创建表头
            sheet.createRow(0).createCell(0).setCellValue("RANK");
            sheet.createRow(0).createCell(1).setCellValue("司机_用户ID");
            sheet.createRow(0).createCell(2).setCellValue("司机_手机号码");
            sheet.createRow(0).createCell(3).setCellValue("司机_姓名");
            sheet.createRow(0).createCell(4).setCellValue("总计完成订单数");
            File file = File.createTempFile(
                    "嘉奖列表_" +
                            SimpleDateFormat.getDateTimeInstance().format(new Date()),
                    ".xlsx"
            );
            int rank = 0;
            // 开始插入数据
            for (PrizeVo prize : prizes){
                // 获取当前行
                int lastRowNum = sheet.getLastRowNum();
                // 创建行
                sheet.createRow(lastRowNum + 1);
                // 创建列
                sheet.getRow(lastRowNum + 1).createCell(0).setCellValue(rank++);
                sheet.getRow(lastRowNum + 1).createCell(1)
                        .setCellValue(prize.driverId());
                sheet.getRow(lastRowNum + 1).createCell(2)
                        .setCellValue(prize.driverPhone());
                sheet.getRow(lastRowNum + 1).createCell(3)
                        .setCellValue(prize.driversName());
                sheet.getRow(lastRowNum + 1).createCell(4)
                        .setCellValue(prize.totalOrders());
            }
            String url = uploadFileToOss(workbook, file);
            return StringUtils.hasText(url) ?
                    R.ok().put("result", url) : R.error("生成订单列表失败");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String uploadFileToOss(Workbook workbook, File file){
        try(
                FileOutputStream fileOutputStream = new FileOutputStream(file)
        ){
            // 保存到OSS 参数为一个File
            workbook.write(fileOutputStream);
            // 上传到OSS
            String url = ossUtil.uploadFile(file);
            if (StringUtils.hasText(url)){
                // 上传成功
                log.info("上传到OSS成功,file:{}", file.getName());
                // 删除临时文件
                file.deleteOnExit();
                return url;
            } else {
                log.error("上传到OSS失败,file:{}", file.getName());
                CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR, "上传到OSS失败");
            }
        } catch (IOException e) {
            log.error("上传到OSS失败,file:{}", file.getName());
            e.printStackTrace();
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR, "上传到OSS失败");
        }
        return null;
    }
}
