package edu.npu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author : [wangminan]
 * @description : [启用AOP]
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true,proxyTargetClass = true)
public class AopConfig {
}
