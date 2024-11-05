package com.hmall.api.client.fallback;

import com.hmall.api.client.TradeClient;
import com.hmall.common.exception.BizIllegalException;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author liuyichen
 * @version 1.0
 */
public class TradeClientFallBack implements FallbackFactory<TradeClient> {
    public TradeClient create(Throwable throwable) {
        return new TradeClient() {

            @Override
            public void markOrderPaySuccess(Long orderId) {
                //更新状态失败
                throw new BizIllegalException(throwable);
            }
        };
    }
}
