package edu.npu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author : [wangminan]
 * @description : [允许启用代理]
 */
@Configuration
// 不知道为什么这个配置写在主函数上面不生效，只能写在这里 记得在pom里配置对AOP的支持
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class AopConfig {
}
