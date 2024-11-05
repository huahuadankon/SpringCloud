package com.hmall.cart;

import com.hmall.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(value = "com.hmall.api.client",defaultConfiguration = DefaultFeignConfig.class)
@MapperScan("com.hmall.cart.mapper")//这里的 @MapperScan("com.hmall.mapper") 告诉 Spring
// 在 com.hmall.mapper 包下扫描所有的 Mapper 接口，即使这些接口没有显式添加 @Mapper 注解。
@SpringBootApplication
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}