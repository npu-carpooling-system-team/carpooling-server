package edu.npu.util;


import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import darabonba.core.client.ClientOverrideConfiguration;
import edu.npu.exception.CarpoolingException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class SendSmsUtil {

    @Resource
    private Environment config;

    @Resource
    private ObjectMapper objectMapper;

    private static final String SMS_ERR_MSG = "send sms error: ";

    public void sendSmsCode(String phoneNumber, String code){

        // Configure Credentials authentication information, including ak, secret, token
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(config.getProperty("aliyun-sms.access.accessKeyId"))
                .accessKeySecret(config.getProperty("aliyun-sms.access.accessKeySecret"))
                //.securityToken("<your-token>") // use STS token
                .build());

        // Configure the Client
        AsyncClient client = AsyncClient.builder()
                .region(config.getProperty("aliyun-sms.async-client.region")) // Region ID
                //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
                .credentialsProvider(provider)
                //.serviceConfiguration(Configuration.create()) // Service-level configuration
                // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride(config.getProperty("aliyun-sms.async-client.endpoint"))
                        //.setConnectTimeout(Duration.ofSeconds(30))
                )
                .build();

        // Parameter settings for API request
        SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                .signName(config.getProperty("aliyun-sms.sms.signName"))
                .templateCode(config.getProperty("aliyun-sms.sms.templateCode"))
                .phoneNumbers(phoneNumber)
                .templateParam("{\"code\":\"" + code + "\"}")
                // Request-level configuration rewrite, can set Http request parameters, etc.
                // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                .build();

        // Asynchronously get the return value of the API request
        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
        // Synchronously get the return value of the API request
        SendSmsResponse resp;
        try {
            resp = response.get();
            if(!(resp.getBody().getCode().equals("OK") && resp.getBody().getMessage().equals("OK"))){
                log.error(SMS_ERR_MSG + objectMapper.writeValueAsString(resp));
                CarpoolingException.cast("send sms error");
            } else {
                log.info("短信发送成功");
            }
        } catch (ExecutionException | JsonProcessingException e) {
            log.error(SMS_ERR_MSG + e.getMessage());
            throw new CarpoolingException(e.getMessage());
        } catch (InterruptedException e){
            log.error(SMS_ERR_MSG + e.getMessage());
            Thread.currentThread().interrupt();
        }

        // Finally, close the client
        client.close();
    }

}
