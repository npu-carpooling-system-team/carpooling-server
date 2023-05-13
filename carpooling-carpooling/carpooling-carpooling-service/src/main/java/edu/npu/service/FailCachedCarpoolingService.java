package edu.npu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.npu.common.UnCachedOperationEnum;
import edu.npu.entity.FailCachedCarpooling;

/**
* @author wangminan
* @description 针对表【fail_cached_carpooling】的数据库操作Service
* @createDate 2023-05-13 11:42:08
*/
public interface FailCachedCarpoolingService extends IService<FailCachedCarpooling> {

    void syncFailCachedCarpooling(int shardIndex, int shardTotal, int i);

    <ID> void saveCachedFileLogToDb(ID carpoolingId, UnCachedOperationEnum operationEnum);
}
