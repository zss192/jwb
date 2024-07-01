package com.jwb;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 内容管理启动类
 */

@SpringBootApplication(scanBasePackages = "com.jwb")
@EnableSwagger2Doc
@EnableFeignClients(basePackages = {"com.jwb.content.feignclient"})
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}
