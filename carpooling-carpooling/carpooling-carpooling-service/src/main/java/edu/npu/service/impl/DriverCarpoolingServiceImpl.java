package edu.npu.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.common.ResponseCodeEnum;
import edu.npu.common.UnCachedOperationEnum;
import edu.npu.doc.CarpoolingDoc;
import edu.npu.dto.AddCarpoolingDto;
import edu.npu.dto.EditCarpoolingDto;
import edu.npu.dto.PageQueryDto;
import edu.npu.entity.Carpooling;
import edu.npu.entity.Driver;
import edu.npu.entity.FailCachedCarpooling;
import edu.npu.entity.LoginAccount;
import edu.npu.exception.CarpoolingException;
import edu.npu.feignClient.DriverServiceClient;
import edu.npu.feignClient.OrderServiceClient;
import edu.npu.mapper.CarpoolingMapper;
import edu.npu.mapper.FailCachedCarpoolingMapper;
import edu.npu.service.DriverCarpoolingService;
import edu.npu.util.RedisClient;
import edu.npu.vo.PageResultVo;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static edu.npu.common.EsConstants.CARPOOLING_INDEX;
import static edu.npu.common.RedisConstants.CACHE_CARPOOLING_KEY;
import static edu.npu.common.RedisConstants.CACHE_CARPOOLING_TTL;

/**
 * @author wangminan
 * @description 针对表【carpooling(拼车行程表)】的数据库操作Service实现
 * @createDate 2023-04-17 19:50:53
 */
@Service
@Slf4j
public class DriverCarpoolingServiceImpl extends ServiceImpl<CarpoolingMapper, Carpooling>
        implements DriverCarpoolingService {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private DriverServiceClient driverServiceClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EsService esService;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private OrderServiceClient orderServiceClient;

    @Resource
    private RedisClient redisClient;

    @Resource
    private FailCachedCarpoolingMapper failCachedCarpoolingMapper;

    // 负责执行新线程上其他任务的线程池
    private static final ExecutorService cachedThreadPool =
        Executors.newFixedThreadPool(
                // 获取系统核数
                Runtime.getRuntime().availableProcessors()
        );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addCarpooling(AddCarpoolingDto addCarpoolingDto, LoginAccount loginAccount) {
        if (addCarpoolingDto.price() < 0) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "价格不能为负数");
        }
        // 从loginAccount中获取driverId addCarpoolingDto中获取其他信息
        Driver driver = driverServiceClient.getDriverByAccountUsername(
                loginAccount.getUsername()
        );
        // 预校验
        // 1. 驾照类型为C1或C2,最多只能搭载6人
        if((driver.getDriversLicenseType().equals("C1") ||
                driver.getDriversLicenseType().equals("C2")) &&
                addCarpoolingDto.totalPassengerNo() > 6
        ){
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "不允许新增行程,您的驾照类型为C1或C2,最多只能搭载6人");
        }
        // 2. addCarPoolingDto中的出发时间不能早于当前时间
        if (addCarpoolingDto.departureTime().before(new Date())) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "不允许新增行程,出发时间不能早于当前时间");
        }
        // 3. addCarpoolingDto中的出发时间不能晚于到达时间
        if (addCarpoolingDto.departureTime().after(addCarpoolingDto.arriveTime())) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "不允许新增行程,出发时间不能晚于到达时间");
        }
        Carpooling carpooling = new Carpooling();
        BeanUtils.copyProperties(addCarpoolingDto, carpooling);
        carpooling.setDriverId(driver.getDriverId());
        // MySQL
        boolean saveMySQL = save(carpooling);
        if (!saveMySQL) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "新增拼车行程失败,MySQL数据库操作失败,请检查参数合法性");
        }
        // 新起一个线程
        cachedThreadPool.execute(
            () -> {
                // ElasticSearch
                boolean saveEs = esService.saveCarpoolingToEs(carpooling);
                if (!saveEs) {
                    log.error("直接新增拼车行程:{}失败,ElasticSearch数据库操作失败,持久化入数据库。",
                            carpooling);
                    failCachedCarpoolingMapper.insert(
                            new FailCachedCarpooling(carpooling.getId(),
                                    UnCachedOperationEnum.INSERT.getValue())
                    );
                }
                try{
                    // Redis 缓存预热
                    redisClient.setWithLogicalExpire(
                            CACHE_CARPOOLING_KEY, carpooling.getId(),
                            carpooling,
                            CACHE_CARPOOLING_TTL, TimeUnit.MINUTES
                    );
                } catch (Exception e) {
                    log.error("直接新增拼车行程:{}失败,Redis缓存操作失败,持久化入数据库。", carpooling);
                    // 防止重复存储
                    if (saveEs) {
                        failCachedCarpoolingMapper.insert(
                                new FailCachedCarpooling(carpooling.getId(),
                                        UnCachedOperationEnum.INSERT.getValue())
                        );
                    }
                }
            }
        );
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateCarpooling(Long id,
                              EditCarpoolingDto editCarpoolingDto,
                              LoginAccount loginAccount) {
        // 预校验
        if (editCarpoolingDto.totalPassengerNo() < editCarpoolingDto.leftPassengerNo()
        ) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "不允许更改行程,您设置的剩余座位数不能大于总座位数");
        } else if (!Objects.equals(id, editCarpoolingDto.id())) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "不允许更改行程,您设置的id与请求路径中的id不一致");
        } else if (editCarpoolingDto.price() < 0) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED, "价格不能为负数");
        }
        // 需要同时修改MySQL和ElasticSearch
        // 从loginAccount中获取driverId addCarpoolingDto中获取其他信息
        Driver driver = driverServiceClient.getDriverByAccountUsername(
                loginAccount.getUsername()
        );
        if (driver.getDriversLicenseType().startsWith("C") &&
                editCarpoolingDto.totalPassengerNo() > 4) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "不允许更改行程,您设置的总座位数不能大于等于4");
        }
        Date departureTime = editCarpoolingDto.departureTime();
        // 如果出发时间为当前时间六小时内 同时有乘客
        if (departureTime.before(DateUtil.offsetHour(new Date(), 6))
                && orderServiceClient.checkHasPassenger(id)
        ) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "不允许更改行程,出发前6小时内且有乘客时不允许更改行程");
        }
        // 不知道为什么用BeanUtils.copyProperties()都是空的
        Carpooling carpooling = new Carpooling();
        carpooling.setDriverId(driver.getDriverId());
        BeanUtils.copyProperties(editCarpoolingDto, carpooling);
        // ES 同样新起一个线程来避免阻塞主线程
        cachedThreadPool.execute(() -> {
            boolean saveEs = esService.updateCarpoolingToEs(carpooling);
            if (!saveEs) {
                log.error("直接修改拼车行程:{}失败,ElasticSearch数据库操作失败,持久化入数据库。",
                        carpooling);
                failCachedCarpoolingMapper.insert(
                        new FailCachedCarpooling(carpooling.getId(),
                                UnCachedOperationEnum.UPDATE.getValue())
                );
            }
            try {
                // 更新Redis缓存
                redisClient.setWithLogicalExpire(
                        CACHE_CARPOOLING_KEY, carpooling.getId(),
                        carpooling,
                        CACHE_CARPOOLING_TTL, TimeUnit.MINUTES
                );
            } catch (Exception e) {
                log.error("直接修改拼车行程:{}失败,Redis缓存操作失败,持久化入数据库。", carpooling);
                if (saveEs) {
                    failCachedCarpoolingMapper.insert(
                            new FailCachedCarpooling(carpooling.getId(),
                                    UnCachedOperationEnum.UPDATE.getValue())
                    );
                }
            }
            // 远程调用,发送通知邮件
            orderServiceClient
                .sendNoticeMailToUser(carpooling.getId(),
                        "您的行程已被修改,请注意查看",
                        "请及时登录西工大拼车平台查看您的行程改动信息,行程编号:"
                                + carpooling.getId()
                );
        });
        // MySQL
        boolean saveMySQL = updateById(carpooling);
        if (!saveMySQL) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "修改拼车行程失败,MySQL数据库操作失败,请确认参数合法性");
        }
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteCarpooling(Long id, LoginAccount loginAccount) {
        // 校验一致性
        Driver driver = driverServiceClient.getDriverByAccountUsername(
                loginAccount.getUsername()
        );
        Carpooling carpooling = getById(id);
        if (!Objects.equals(driver.getDriverId(), carpooling.getDriverId())) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "不允许删除行程,您不是该行程的发布者");
        }
        // 发车前六小时 同时有乘客不允许删除
        if (carpooling.getDepartureTime()
                .before(DateUtil.offsetHour(new Date(), 6))
                && orderServiceClient.checkHasPassenger(id)
        ) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "不允许删除行程,出发前6小时内且有乘客时不允许删除行程");
        }
        // MySQL和ES都要删掉
        // ES 还是要新起一个线程来避免阻塞主线程
        cachedThreadPool.execute(() -> {
            boolean removeEs = esService.deleteCarpoolingFromEs(id);
            if (!removeEs) {
                log.error("直接删除拼车行程:{}失败,ElasticSearch数据库操作失败,持久化入数据库。",
                        carpooling);
                failCachedCarpoolingMapper.insert(
                        new FailCachedCarpooling(carpooling.getId(),
                                UnCachedOperationEnum.DELETE.getValue())
                );
            }
            try {
                // 刷新Redis 删除
                stringRedisTemplate.delete(CACHE_CARPOOLING_KEY + id);
            } catch (Exception e) {
                log.error("直接删除拼车行程:{}失败,Redis缓存操作失败,持久化入数据库。", carpooling);
                if (removeEs) {
                    failCachedCarpoolingMapper.insert(
                        new FailCachedCarpooling(carpooling.getId(),
                            UnCachedOperationEnum.DELETE.getValue())
                    );
                }
            }
            // 发送通知邮件
            orderServiceClient
                    .sendNoticeMailToUser(
                            carpooling.getId(),
                            "您的行程已被删除,请注意查看",
                            "请及时登录西工大拼车平台查看您的行程删除信息,行程编号:"
                                    + carpooling.getId()
                    );
            // 调用远程服务更改状态
            orderServiceClient.forceCloseOrderByCarpoolingId(id);
        });
        // MySQL
        boolean removeMySQL = removeById(id);
        if (!removeMySQL) {
            return R.error(ResponseCodeEnum.PRE_CHECK_FAILED,
                    "删除拼车行程失败,MySQL数据库操作失败,请确认参数合法性");
        }
        return R.ok();
    }

    @Override
    public R getCarpooling(PageQueryDto pageQueryDto, LoginAccount loginAccount) {
        // 需要从ES中检索数据 同时注意分页 我们这种数据量较小的情况可以使用from-size方式
        Driver driver = driverServiceClient.getDriverByAccountUsername(
                loginAccount.getUsername()
        );
        try {
            // 1.准备Request
            SearchRequest request = new SearchRequest(CARPOOLING_INDEX);
            // 2.准备请求参数
            // 2.1.query
            buildBasicQuery(driver.getDriverId(), pageQueryDto, request);
            // 2.2.分页
            int page = pageQueryDto.getPageNum();
            int size = pageQueryDto.getPageSize();
            request.source().from((page - 1) * size).size(size);
            // 3.发送请求
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            // 4.解析响应
            return resolveRestResponse(response);
        } catch (IOException e) {
            CarpoolingException.cast("获取拼车行程失败");
        }
        return R.ok("数据为空");
    }

    @Override
    public R resolveRestResponse(SearchResponse response) {
        PageResultVo pageResult = handlePageResponse(response);
        R r = new R();
        r.put("code", ResponseCodeEnum.SUCCESS.getValue());
        r.put("total", pageResult.total());
        r.put("data", pageResult.data());
        return r;
    }

    @Override
    public void buildBasicQuery(Long driverId, PageQueryDto pageQueryDto, SearchRequest searchRequest) {
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(pageQueryDto);
        // 3. DriverId一致
        boolQueryBuilder.must(QueryBuilders.termQuery("driverId", driverId));
        // 拼装 排序时事件从近到远
        searchRequest.source().query(boolQueryBuilder).sort(
                SortBuilders.fieldSort("departureTime").order(SortOrder.ASC)
        );
    }

    private PageResultVo handlePageResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        // 4.1.总条数
        long total = searchHits.getTotalHits().value;
        // 4.2.获取文档数组
        SearchHit[] hits = searchHits.getHits();
        // 4.3.遍历
        List<CarpoolingDoc> carpoolingDocs = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            // 4.4.获取source
            String json = hit.getSourceAsString();
            // 4.5.反序列化
            CarpoolingDoc carpoolingDoc
                    = objectMapper.convertValue(json, CarpoolingDoc.class);
            // 4.9.放入集合
            carpoolingDocs.add(carpoolingDoc);
        }
        return new PageResultVo(total, carpoolingDocs);
    }

    @Override
    public void buildBasicQuery(PageQueryDto pageQueryDto, SearchRequest searchRequest) {
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(pageQueryDto);
        // 拼装
        searchRequest.source().query(boolQueryBuilder).sort(
                SortBuilders.fieldSort("departureTime").order(SortOrder.ASC)
        );
    }

    private static BoolQueryBuilder getBoolQueryBuilder(PageQueryDto pageQueryDto) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 1.关键字
        String keyword = pageQueryDto.getQuery();
        if (StringUtils.hasText(keyword)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("all", keyword));
        } else {
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }
        // 2.时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date departureTime = pageQueryDto.getDepartureTime();
        if (departureTime != null) {
            String departureTimeStr = sdf.format(departureTime);
            boolQueryBuilder.must(QueryBuilders.rangeQuery("departureTime")
                    .gte(departureTimeStr));
        }
        Date arriveTime = pageQueryDto.getArriveTime();
        if (arriveTime != null) {
            String arriveTimeStr = sdf.format(arriveTime);
            boolQueryBuilder.must(QueryBuilders.rangeQuery("arriveTime")
                    .lte(arriveTimeStr));
        }
        return boolQueryBuilder;
    }
}
