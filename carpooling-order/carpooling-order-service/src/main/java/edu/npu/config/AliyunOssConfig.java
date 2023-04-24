package edu.npu.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author : [wangminan]
 * @description : [统一配置OssClient并注入环境]
 */
@Configuration
public class AliyunOssConfig {

    @Resource
    private Environment config;

    @Bean
    public OSS oss(){
        return new OSSClientBuilder().build(
                config.getProperty("var.aliyun-oss.endpoint"),
                config.getProperty("var.aliyun-oss.accessKeyId"),
                config.getProperty("var.aliyun-oss.accessKeySecret")
        );
    }
}
