package com.hmall.api.client;

import com.hmall.api.client.fallback.TradeClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * @author liuyichen
 * @version 1.0
 */
@FeignClient(value = "order-service",fallbackFactory = TradeClientFallBack.class)
public interface TradeClient {
    @PutMapping("/orders/{orderId}")
    void markOrderPaySuccess(@PathVariable("orderId") Long orderId);
}
