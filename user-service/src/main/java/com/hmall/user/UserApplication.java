package com.hmall.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.hmall.user.mapper")//这里的 @MapperScan("com.hmall.mapper") 告诉 Spring
// 在 com.hmall.mapper 包下扫描所有的 Mapper 接口，即使这些接口没有显式添加 @Mapper 注解。
@SpringBootApplication
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}