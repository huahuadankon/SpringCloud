package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author liuyichen
 * @version 1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TradeStatusListener {
    private final ICartService cartService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue",durable = "true"),
            exchange = @Exchange(name = "trade.topic",type = "topic"),
            key = "order.create"
    ))
    public void listenCartClearQueue(Message message,Set<Long> ids) {
        log.info("cart clear queue");

        Long userId = message.getMessageProperties().getHeader("user-info");
        UserContext.setUser(userId);

        cartService.removeByItemIds(ids);

    }
}
