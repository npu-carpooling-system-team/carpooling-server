package edu.npu.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.npu.doc.CarpoolingDoc;
import edu.npu.entity.Carpooling;
import edu.npu.exception.CarpoolingError;
import edu.npu.exception.CarpoolingException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static edu.npu.common.EsConstants.CARPOOLING_INDEX;

/**
 * @author : [wangminan]
 * @description : [处理ES中的增删改,另起一个类是因为这些方法放在原来的类里多线程下会导致事务失效]
 */
@Service
@Slf4j
public class EsService {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Transactional(rollbackFor = Exception.class)
    public boolean saveCarpoolingToEs(Carpooling carpooling) {
        log.info("开始保存carpooling:{}到ElasticSearch", carpooling);
        CarpoolingDoc carpoolingDoc = new CarpoolingDoc(carpooling);
        String jsonDoc = null;
        try {
            jsonDoc = objectMapper.writeValueAsString(carpoolingDoc);
        } catch (JsonProcessingException e) {
            log.error("carpooling对象:{}无法转换为json字符串", carpooling);
            
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR,
                    "carpooling对象无法转换为json字符串");
        }
        if (StrUtil.isEmpty(jsonDoc)) {
            log.error("carpooling对象:{}无法转换为json字符串", carpooling);
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR,
                    "carpooling对象无法转换为json字符串");
        }
        // 1.准备Request
        IndexRequest request = new IndexRequest(CARPOOLING_INDEX)
                .id(String.valueOf(carpoolingDoc.getId()));
        // 2.准备请求参数DSL，其实就是文档的JSON字符串
        request.source(jsonDoc, XContentType.JSON);
        // 3.发送请求
        IndexResponse index = null;
        try {
            index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("新增拼车行程失败,carpoolingDoc:{}", carpoolingDoc);
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR,
                    "新增拼车行程失败,ES出错");
        }
        // 判断返回状态
        if (index == null || !index.status().equals(RestStatus.CREATED)) {
            log.error("新增拼车行程失败,carpoolingDoc:{},", carpoolingDoc);
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR,
                    "新增拼车行程失败,ES出错");
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateCarpoolingToEs(Carpooling carpooling) {
        CarpoolingDoc carpoolingDoc = new CarpoolingDoc(carpooling);
        // 1.准备Request
        UpdateRequest request =
                new UpdateRequest(
                        CARPOOLING_INDEX,
                        String.valueOf(carpoolingDoc.getId()));
        // 2.准备参数
        String jsonDoc = null;
        try {
            jsonDoc = objectMapper.writeValueAsString(carpoolingDoc);
        } catch (JsonProcessingException e) {
            log.error("carpooling对象:{}无法转换为json字符串", carpooling);
            
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR,
                    "修改拼车行程失败,ES出错");
        }
        request.doc(jsonDoc, XContentType.JSON);
        UpdateResponse updateResponse = null;
        // 3.发送请求
        try {
            updateResponse =
                    restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("修改拼车行程失败,ES出错,carpoolingDoc:{}", carpoolingDoc);
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR,
                    "修改拼车行程失败,ES出错");
        }
        if (updateResponse == null || !updateResponse.status().equals(RestStatus.OK)) {
            log.error("修改拼车行程失败,carpoolingDoc:{},返回值:{}", carpoolingDoc, updateResponse);
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR,
                    "修改拼车行程失败,ES出错");
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCarpoolingFromEs(Long id) {
        // 1.准备Request      // DELETE /hotel/_doc/{id}
        DeleteRequest request = new DeleteRequest(CARPOOLING_INDEX, String.valueOf(id));
        // 2.发送请求
        DeleteResponse deleteResponse = null;
        try {
            deleteResponse =
                    restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("删除拼车行程失败,ES出错,id:{}", id);
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR,
                    "删除拼车行程失败,ES出错");
        }
        if (deleteResponse == null || !deleteResponse.status().equals(RestStatus.OK)) {
            log.error("删除拼车行程失败,id:{},返回值:{}", id, deleteResponse);
            CarpoolingException.cast(CarpoolingError.UNKNOWN_ERROR,
                    "删除拼车行程失败");
        }
        return true;
    }
}
