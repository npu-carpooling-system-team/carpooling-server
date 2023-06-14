package edu.npu.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.doc.CarpoolingDoc;
import edu.npu.dto.PageQueryDto;
import edu.npu.entity.Carpooling;
import edu.npu.exception.CarpoolingException;
import edu.npu.mapper.CarpoolingMapper;
import edu.npu.service.DriverCarpoolingService;
import edu.npu.service.PassengerCarpoolingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Service
@Slf4j
public class PassengerCarpoolingServiceImpl extends ServiceImpl<CarpoolingMapper, Carpooling>
        implements PassengerCarpoolingService {

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private DriverCarpoolingService driverCarpoolingService;

    // 在这个位置仅返回行程的基础数据 在进入详情页面时执行二次请求。二次请求的过程可以走两级搜索的模式
    @Override
    public R getCarpoolingList(PageQueryDto pageQueryDto) {
        // 处理pageQueryDto 如果出发时间早于等于当前时间则设定其为当前时间
        if (pageQueryDto.getDepartureTime() == null ||
                pageQueryDto.getDepartureTime().before(new Date())) {
            pageQueryDto.setDepartureTime(new Date());
        }
        // 需要从ES中检索数据 同时注意分页 我们这种数据量较小的情况可以使用from-size方式
        try {
            // 1.准备Request
            // 2.准备请求参数
            // 2.1.query
            SearchRequest request = driverCarpoolingService.buildBasicQuery(pageQueryDto);
            // 2.2.分页
            // 3.发送请求
            SearchResponse<CarpoolingDoc> response =
                    elasticsearchClient.search(request, CarpoolingDoc.class);
            // 4.解析响应
            return driverCarpoolingService.resolveRestResponse(response);
        } catch (IOException e) {
            throw new CarpoolingException("获取拼车列表失败");
        }
    }
}
