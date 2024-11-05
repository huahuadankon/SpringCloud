package com.hmall.common.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liuyichen
 * @version 1.0
 */
@Configuration
public class MqConfig {

   @Bean
    public MessageConverter jackson2JsonMessageConverter() {

       return new Jackson2JsonMessageConverter();
   }

}
