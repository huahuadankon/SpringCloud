package com.hmall.api.client;

import com.hmall.api.client.fallback.ItemClientFallBack;
import com.hmall.api.config.DefaultFeignConfig;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * 通过公用的接口指定请求的服务和地址，再用通过spring生成代理类，得到数据,类似于mapper
 */
@FeignClient(value = "item-service" ,fallbackFactory = ItemClientFallBack.class)
public interface ItemClient {
       @GetMapping("/items")
       List<ItemDTO> queryItemsByIds(@RequestParam("ids") Collection<Long> ids);


       @PutMapping("/items/stock/deduct")
       void deductStock(@RequestBody List<OrderDetailDTO> orderDetailDTOS);

       @PutMapping("/items/stock/restore")
       void recoverStore(List<OrderDetailDTO> orderDetails);
}
