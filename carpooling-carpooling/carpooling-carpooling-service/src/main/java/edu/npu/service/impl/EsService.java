package edu.npu.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.common.UnCachedOperationEnum;
import edu.npu.doc.CarpoolingDoc;
import edu.npu.entity.Carpooling;
import edu.npu.service.FailCachedCarpoolingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static edu.npu.common.EsConstants.CARPOOLING_INDEX;

/**
 * @author : [wangminan]
 * @description : [处理ES中的增删改,另起一个类是因为这些方法放在原来的类里多线程下会导致事务失效]
 */
@Service
@Slf4j
public class EsService {

    public static final String CONVERT_CARPOOLING_WARNING_LOG = "carpooling对象:{}无法转换为json字符串";
    @Resource
    private ObjectMapper objectMapper;

    // 新的elasticSearch客户端主要支持两种构造方式 1. lambda 2. builder
    // 在EsService类中我们主要使用lambda的方式
    // 在DriverCarpoolingServiceImpl中我们主要使用builder的方式 都可供参考
    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    @Lazy
    private FailCachedCarpoolingService failCachedCarpoolingService;

    public boolean saveCarpoolingToEs(Carpooling carpooling) {
        log.info("开始保存carpooling:{}到ElasticSearch", carpooling);
        CarpoolingDoc carpoolingDoc = new CarpoolingDoc(carpooling);
        // 3.直接发送请求
        IndexResponse indexResponse = null;
        try {
            indexResponse = elasticsearchClient.index(index ->
                    index.index(CARPOOLING_INDEX)
                            .id(String.valueOf(carpooling.getId()))
                            .document(carpoolingDoc));
        } catch (IOException e) {
            log.error("新增拼车行程失败,carpoolingDoc:{}", carpoolingDoc);
            failCachedCarpoolingService.saveCachedFileLogToDb(carpooling.getId(),
                    UnCachedOperationEnum.INSERT);
        }
        // 判断返回状态
        if (indexResponse == null) {
            log.error("新增拼车行程失败,carpoolingDoc:{},", carpoolingDoc);
            failCachedCarpoolingService.saveCachedFileLogToDb(carpooling.getId(),
                    UnCachedOperationEnum.INSERT);
        }
        return true;
    }

    public boolean updateCarpoolingToEs(Carpooling carpooling) {
        CarpoolingDoc carpoolingDoc = new CarpoolingDoc(carpooling);
        // 1.准备Request
        // 给出两个类型参数 第一个参数是TDocument 表示文档类型 在upsert时被调用
        // 第二个类型参数是TPartialDocument 表示更新文档类型 在doc时被调用
        UpdateRequest<CarpoolingDoc, CarpoolingDoc> updateRequest =
                new UpdateRequest.Builder<CarpoolingDoc, CarpoolingDoc>()
                        .index(CARPOOLING_INDEX)
                        .id(String.valueOf(carpooling.getId()))
                        // upsert表示如果不存在则新增
                        .upsert(carpoolingDoc)
                        .doc(carpoolingDoc)
                        .build();
        UpdateResponse<CarpoolingDoc> updateResponse = null;
        // 3.发送请求
        try {
            updateResponse =
                    elasticsearchClient.update(updateRequest, CarpoolingDoc.class);
        } catch (IOException e) {
            log.error("修改拼车行程失败,ES出错,carpoolingDoc:{}", carpoolingDoc);
            failCachedCarpoolingService.saveCachedFileLogToDb(carpooling.getId(),
                    UnCachedOperationEnum.UPDATE);
        }
        if (updateResponse == null) {
            log.error("修改拼车行程失败,carpoolingDoc:{}", carpoolingDoc);
            failCachedCarpoolingService.saveCachedFileLogToDb(carpooling.getId(),
                    UnCachedOperationEnum.UPDATE);
        }
        return true;
    }

    public boolean deleteCarpoolingFromEs(Long id) {
        // 1.准备Request      // DELETE /hotel/_doc/{id}
        DeleteRequest request = new DeleteRequest.Builder()
                .index(CARPOOLING_INDEX)
                .id(String.valueOf(id))
                .build();
        // 2.发送请求
        DeleteResponse deleteResponse = null;
        try {
            deleteResponse =
                    elasticsearchClient.delete(request);
        } catch (IOException e) {
            log.error("删除拼车行程失败,ES出错,id:{}", id);
            failCachedCarpoolingService.saveCachedFileLogToDb(id,
                    UnCachedOperationEnum.DELETE);
        }
        if (deleteResponse == null) {
            log.error("删除拼车行程失败,id:{},返回值:{}", id, deleteResponse);
            failCachedCarpoolingService.saveCachedFileLogToDb(id,
                    UnCachedOperationEnum.DELETE);
        }
        return true;
    }
}
