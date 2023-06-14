package edu.npu.service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.doc.CarpoolingDoc;
import edu.npu.dto.AddCarpoolingDto;
import edu.npu.dto.EditCarpoolingDto;
import edu.npu.dto.PageQueryDto;
import edu.npu.entity.Carpooling;
import edu.npu.entity.LoginAccount;
import edu.npu.vo.R;
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

    R resolveRestResponse(SearchResponse<CarpoolingDoc> response);

    SearchRequest buildBasicQuery(Long driverId, PageQueryDto pageQueryDto);

    SearchRequest buildBasicQuery(PageQueryDto pageQueryDto);
}
