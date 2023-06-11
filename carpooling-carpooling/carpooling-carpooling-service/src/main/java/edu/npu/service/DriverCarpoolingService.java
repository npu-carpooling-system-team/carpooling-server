package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.dto.AddCarpoolingDto;
import edu.npu.dto.EditCarpoolingDto;
import edu.npu.dto.PageQueryDto;
import edu.npu.entity.Carpooling;
import edu.npu.entity.LoginAccount;
import edu.npu.vo.R;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangminan
 * @description 针对表【carpooling(拼车行程表)】的数据库操作Service
 * @createDate 2023-04-17 19:50:53
 */
public interface DriverCarpoolingService extends IService<Carpooling> {

    @Transactional(rollbackFor = Exception.class)
    R addCarpooling(AddCarpoolingDto addCarpoolingDto, LoginAccount loginAccount);

    @Transactional(rollbackFor = Exception.class)
    R updateCarpooling(Long id, EditCarpoolingDto addCarpoolingDto, LoginAccount loginAccount);

    void updateCarpoolingToNoSQL(Carpooling carpooling);

    @Transactional(rollbackFor = Exception.class)
    R deleteCarpooling(Long id, LoginAccount loginAccount);

    R getCarpooling(PageQueryDto pageQueryDto, LoginAccount loginAccount);

    R resolveRestResponse(SearchResponse response);

    void buildBasicQuery(Long driverId, PageQueryDto pageQueryDto, SearchRequest searchRequest);

    void buildBasicQuery(PageQueryDto pageQueryDto, SearchRequest searchRequest);
}
