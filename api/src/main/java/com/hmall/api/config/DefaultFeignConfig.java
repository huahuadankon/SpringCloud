package com.hmall.api.config;

import com.hmall.api.client.PayClient;
import com.hmall.api.client.fallback.*;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


public class DefaultFeignConfig {


  /*  @Bean
    *//**
     * openFeign日志输出
     *//*
    public Logger.Level feignLogLevel() {
        return Logger.Level.FULL;
    }*/

    @Bean
    /**
     * openFeign的拦截器，实现微服务之间通信时信息的保存
     */
    public RequestInterceptor UserInfoRequestInterceptor() {
        return new RequestInterceptor() {
            public void apply(RequestTemplate template) {
                Long userId = UserContext.getUser();
                if (userId != null) {
                    template.header("user-info", userId.toString());
                }
            }
        };
    }
    @Bean
    public ItemClientFallBack itemClientFallBack() {
        return new ItemClientFallBack();
    }
    @Bean
    public UserClientFallBack userClientFallBack() {
        return new UserClientFallBack();
    }

    @Bean
    public TradeClientFallBack tradeClientFallBack() {
        return new TradeClientFallBack();

    }
    @Bean
    public CartClientFallBack cartClientFallBack() {
        return new CartClientFallBack();
    }

    @Bean
    public PayClientFallback payClientFallBack() {
        return new PayClientFallback();
    }


}
