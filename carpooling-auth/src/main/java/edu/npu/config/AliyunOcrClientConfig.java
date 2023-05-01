package edu.npu.config;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author : [wangminan]
 * @description : [阿里云ocr客户端配置]
 */
@Configuration
public class AliyunOcrClientConfig {

    @Resource
    private Environment config; // 依赖注入

    @Bean
    public Client createClient() throws Exception {
        Config ocrConfig = new Config()
                .setAccessKeyId(config.getProperty("var.aliyun-ocr.accessKeyId"))
                .setAccessKeySecret(config.getProperty("var.aliyun-ocr.accessKeySecret"));
        ocrConfig.setEndpoint(config.getProperty("var.aliyun-ocr.endpoint"));
        return new Client(ocrConfig);
    }
}
