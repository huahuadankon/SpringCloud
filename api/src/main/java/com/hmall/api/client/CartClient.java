package com.hmall.api.client;

import com.hmall.api.client.fallback.CartClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
//启用降级逻辑
@FeignClient(value = "cart-service",fallbackFactory = CartClientFallBack.class)
public interface CartClient {
    @DeleteMapping("/carts")
    void deleteCartItemByIds(@RequestParam("ids") Collection<Long> ids);
}



