package edu.npu.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.doc.CarpoolingDoc;
import edu.npu.dto.AddCarpoolingDto;
import edu.npu.entity.Carpooling;
import edu.npu.entity.Driver;
import edu.npu.entity.LoginAccount;
import edu.npu.feignClient.DriverServiceClient;
import edu.npu.mapper.CarpoolingMapper;
import edu.npu.service.CarpoolingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;

import static edu.npu.common.EsConstants.CARPOOLING_INDEX;

/**
 * @author wangminan
 * @description 针对表【carpooling(拼车行程表)】的数据库操作Service实现
 * @createDate 2023-04-17 19:50:53
 */
@Service
@Slf4j
public class CarpoolingServiceImpl extends ServiceImpl<CarpoolingMapper, Carpooling>
        implements CarpoolingService {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private DriverServiceClient driverServiceClient;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addCarpooling(AddCarpoolingDto addCarpoolingDto, LoginAccount loginAccount) {
        // 从loginAccount中获取driverId addCarpoolingDto中获取其他信息
        Driver driver = driverServiceClient.getDriverWithAccountUsername(
                loginAccount.getUsername()
        );
        // 不知道为什么用BeanUtils.copyProperties()都是空的
        Carpooling carpooling = copyFromCarpoolingDto(addCarpoolingDto, driver);
        // MySQL
        boolean saveMySQL = save(carpooling);
        if (!saveMySQL) {
            return R.error(ResponseCodeEnum.ServerError,
                    "新增拼车行程失败,MySQL数据库操作失败,请检查参数合法性");
        }
        // ElasticSearch 使用代理类防止失效
        boolean saveEs = ((CarpoolingService) AopContext.currentProxy())
                .saveCarpoolingToEs(carpooling);
        if (!saveEs) {
            return R.error(ResponseCodeEnum.ServerError,
                    "新增拼车行程失败,ElasticSearch数据库操作失败");
        }
        return R.ok();
    }

    private static Carpooling copyFromCarpoolingDto(AddCarpoolingDto addCarpoolingDto, Driver driver) {
        Carpooling carpooling = new Carpooling();
        carpooling.setDriverId(driver.getDriverId());
        carpooling.setDescription(addCarpoolingDto.description());
        carpooling.setDeparturePoint(addCarpoolingDto.departurePoint());
        carpooling.setPassingPoint(addCarpoolingDto.passingPoint());
        carpooling.setArrivePoint(addCarpoolingDto.arrivePoint());
        carpooling.setDepartureTime(addCarpoolingDto.departureTime());
        carpooling.setArriveTime(addCarpoolingDto.arriveTime());
        carpooling.setTotalPassengerNo(addCarpoolingDto.totalPassengerNo());
        carpooling.setLeftPassengerNo(addCarpoolingDto.leftPassengerNo());
        carpooling.setPrice(addCarpoolingDto.price());
        return carpooling;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCarpoolingToEs(Carpooling carpooling) {
        log.info("开始保存carpooling:{}到ElasticSearch", carpooling);
        CarpoolingDoc carpoolingDoc = new CarpoolingDoc(carpooling);
        String jsonDoc;
        try {
            jsonDoc = objectMapper.writeValueAsString(carpoolingDoc);
        } catch (JsonProcessingException e) {
            log.error("carpooling对象:{}无法转换为json字符串", carpooling);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (StrUtil.isEmpty(jsonDoc)) {
            log.error("carpooling对象:{}无法转换为json字符串", carpooling);
            throw new RuntimeException("carpooling对象无法转换为json字符串");
        }
        // 1.准备Request
        IndexRequest request = new IndexRequest(CARPOOLING_INDEX)
                .id(String.valueOf(carpoolingDoc.getId()));
        // 2.准备请求参数DSL，其实就是文档的JSON字符串
        request.source(jsonDoc, XContentType.JSON);
        // 3.发送请求
        IndexResponse index;
        try {
            index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("新增拼车行程失败,carpoolingDoc:{}", carpoolingDoc);
            throw new RuntimeException(e);
        }
        // 判断返回状态
        if (index == null || !index.status().equals(RestStatus.CREATED)) {
            log.error("新增拼车行程失败,carpoolingDoc:{},", carpoolingDoc);
            throw new RuntimeException("新增拼车行程失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateCarpooling(AddCarpoolingDto addCarpoolingDto, LoginAccount loginAccount) {
        // 需要同时修改MySQL和ElasticSearch
        // 从loginAccount中获取driverId addCarpoolingDto中获取其他信息
        Driver driver = driverServiceClient.getDriverWithAccountUsername(
                loginAccount.getUsername()
        );
        Date departureTime = addCarpoolingDto.departureTime();
        // 如果出发时间为当前时间六小时内则不允许修改
        if (departureTime.before(DateUtil.offsetHour(new Date(), 6))) {
            return R.error(ResponseCodeEnum.ServerError,
                    "不允许更改行程,您设置的出发时间必须在当前时间六小时之后");
        }
        // 不知道为什么用BeanUtils.copyProperties()都是空的
        Carpooling carpooling = copyFromCarpoolingDto(addCarpoolingDto, driver);
        // MySQL
        boolean saveMySQL = update(carpooling,
                new LambdaUpdateWrapper<Carpooling>()
                        .eq(Carpooling::getDriverId, driver.getDriverId())
                        .eq(Carpooling::getDepartureTime, departureTime));
        if (!saveMySQL) {
            return R.error(ResponseCodeEnum.ServerError,
                    "修改拼车行程失败,MySQL数据库操作失败,请确认参数合法性");
        }
        // 查mysql 获取ID
        carpooling = getOne(
                new LambdaQueryWrapper<Carpooling>()
                        .eq(Carpooling::getDriverId, driver.getDriverId())
                        .eq(Carpooling::getDepartureTime, departureTime)
        );
        // ES
        boolean saveEs = ((CarpoolingService) AopContext.currentProxy())
                .updateCarpoolingToEs(carpooling);
        if (!saveEs) {
            return R.error(ResponseCodeEnum.ServerError,
                    "修改拼车行程失败,ElasticSearch数据库操作失败");
        }
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCarpoolingToEs(Carpooling carpooling) {
        CarpoolingDoc carpoolingDoc = new CarpoolingDoc(carpooling);
        // 1.准备Request
        UpdateRequest request =
                new UpdateRequest(
                        CARPOOLING_INDEX,
                        String.valueOf(carpoolingDoc.getId()));
        // 2.准备参数
        String jsonDoc;
        try {
            jsonDoc = objectMapper.writeValueAsString(carpoolingDoc);
        } catch (JsonProcessingException e) {
            log.error("carpooling对象:{}无法转换为json字符串", carpooling);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        request.doc(jsonDoc, XContentType.JSON);
        UpdateResponse updateResponse;
        // 3.发送请求
        try {
            updateResponse =
                    restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("修改拼车行程失败,ES出错,carpoolingDoc:{}", carpoolingDoc);
            throw new RuntimeException(e);
        }
        if (updateResponse == null || !updateResponse.status().equals(RestStatus.OK)) {
            log.error("修改拼车行程失败,carpoolingDoc:{},返回值:{}", carpoolingDoc, updateResponse);
            throw new RuntimeException("修改拼车行程失败");
        }
        return true;
    }
}




