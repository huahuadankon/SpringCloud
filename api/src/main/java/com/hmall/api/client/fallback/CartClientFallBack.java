package com.hmall.api.client.fallback;

import com.hmall.api.client.CartClient;
import com.hmall.common.exception.BizIllegalException;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;

/**
 * @author liuyichen
 * @version 1.0
 */
public class CartClientFallBack implements FallbackFactory<CartClient> {

    @Override
    public CartClient create(Throwable cause) {
       return new CartClient() {

           @Override
           public void deleteCartItemByIds(Collection<Long> ids) {
               // 库存扣减业务需要触发事务回滚，查询失败，抛出异常
               throw new BizIllegalException(cause);
           }
       };
    }
}
