package com.hmall.api.client;

import com.hmall.api.client.fallback.UserClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

/**
 * @author liuyichen
 * @version 1.0
 */
@FeignClient(value = "user-service",fallbackFactory = UserClientFallBack.class)
public interface UserClient {
    @PutMapping("/users/money/deduct")
    void deductMoney(@RequestParam("pw") String  pw ,@RequestParam("amount") Integer amount);
}
