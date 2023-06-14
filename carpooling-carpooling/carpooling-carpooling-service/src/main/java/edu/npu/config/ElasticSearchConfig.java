package edu.npu.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author : [wangminan]
 * @description : [ES配置类]
 */
@Configuration
@Slf4j
public class ElasticSearchConfig {
    @Resource
    private Environment config;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 注意 ES8.2版本默认开启了SSL与安全认证，需要在配置文件中关闭
     * @return ElasticsearchClient
     */
    @Bean
    public ElasticsearchClient client() {
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(
                        config.getProperty(
                                "var.elasticsearch.username",
                                "elastic"),
                        config.getProperty(
                                "var.elasticsearch.password",
                                "123456")));
        RestClient restClient = RestClient.builder(
                        new HttpHost(
                                config.getProperty(
                                        "var.elasticsearch.host",
                                        "8.218.84.229"),
                                Integer.parseInt(config.getProperty(
                                        "var.elasticsearch.port",
                                        "9200")))
                        )
                .setHttpClientConfigCallback(
                        httpClientBuilder ->
                                httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
                ).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper));
        return new ElasticsearchClient(transport);
    }
}
