package edu.npu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.npu.dto.PageQueryDto;
import edu.npu.entity.Carpooling;
import edu.npu.exception.CarpoolingException;
import edu.npu.mapper.CarpoolingMapper;
import edu.npu.service.DriverCarpoolingService;
import edu.npu.service.PassengerCarpoolingService;
import edu.npu.vo.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static edu.npu.common.EsConstants.CARPOOLING_INDEX;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
@Service
@Slf4j
public class PassengerCarpoolingServiceImpl extends ServiceImpl<CarpoolingMapper, Carpooling>
        implements PassengerCarpoolingService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private DriverCarpoolingService driverCarpoolingService;

    // TODO 为了加快前端有大量数据情况下的加载速度 这个接口和司机的搜索接口可以做优化
    // 在这个位置仅返回行程的基础数据 在进入详情页面时执行二次请求。二次请求的过程可以走两级搜索的模式
    @Override
    public R getCarpoolingList(PageQueryDto pageQueryDto) {
        // 需要从ES中检索数据 同时注意分页 我们这种数据量较小的情况可以使用from-size方式
        try {
            // 1.准备Request
            SearchRequest request = new SearchRequest(CARPOOLING_INDEX);
            // 2.准备请求参数
            // 2.1.query
            driverCarpoolingService.buildBasicQuery(pageQueryDto, request);
            // 2.2.分页
            int page = pageQueryDto.getPageNum();
            int size = pageQueryDto.getPageSize();
            request.source().from((page - 1) * size).size(size);
            // 3.发送请求
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            // 4.解析响应
            return driverCarpoolingService.resolveRestResponse(response);
        } catch (IOException e) {
            CarpoolingException.cast("获取拼车行程失败");
        }
        return R.ok("数据为空");
    }
}
