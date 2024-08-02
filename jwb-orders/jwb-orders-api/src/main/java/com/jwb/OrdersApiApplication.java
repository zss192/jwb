package com.jwb;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableSwagger2Doc
@SpringBootApplication(scanBasePackages = "com.jwb")
@EnableFeignClients(basePackages = {"com.jwb.orders.feignclient"})
public class OrdersApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersApiApplication.class, args);
    }

}
