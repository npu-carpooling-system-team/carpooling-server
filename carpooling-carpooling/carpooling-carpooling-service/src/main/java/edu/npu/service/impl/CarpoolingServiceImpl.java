package edu.npu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
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
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

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
    public R addCarpooling(AddCarpoolingDto addCarpoolingDto, LoginAccount loginAccount) {
        // 从loginAccount中获取driverId addCarpoolingDto中获取其他信息
        Driver driver = driverServiceClient.getDriverWithAccountUsername(
                loginAccount.getUsername()
        );
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
        log.info("carpooling:{}", carpooling);
        // 先写到MySQL 再写到ElasticSearch 完成新增过程
        log.info("addCarpoolingDto:{}", addCarpoolingDto);
        // MySQL
        boolean saveMySQL = save(carpooling);
        if (!saveMySQL) {
            return R.error(ResponseCodeEnum.ServerError,
                    "新增拼车行程失败,MySQL数据库操作失败");
        }
        // ElasticSearch 使用代理类防止失效
        boolean saveEs = ((CarpoolingServiceImpl) AopContext.currentProxy())
                .saveCarpoolingToEs(carpooling);
        if (!saveEs) {
            return R.error(ResponseCodeEnum.ServerError,
                    "新增拼车行程失败,ElasticSearch数据库操作失败");
        }
        return R.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveCarpoolingToEs(Carpooling carpooling) {
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
        if (index == null) {
            log.error("新增拼车行程失败,carpoolingDoc:{}", carpoolingDoc);
            throw new RuntimeException("新增拼车行程失败");
        }
        // 判断返回状态
        if (index.status().equals(RestStatus.CREATED)) {
            log.error("新增拼车行程失败,carpoolingDoc:{}", carpoolingDoc);
            throw new RuntimeException("新增拼车行程失败");
        }
        return true;
    }
}




