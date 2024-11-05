package com.hmall.api.client.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author liuyichen
 * @version 1.0
 * 失败降级处理。
 */
@Slf4j
public class ItemClientFallBack implements FallbackFactory<ItemClient> {

    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {

            @Override
            public List<ItemDTO> queryItemsByIds(Collection<Long> ids) {
                log.error("远程调用ItemClient#queryItemByIds方法出现异常，参数：{}", ids, cause);
                // 查询购物车允许失败，查询失败，返回空集合
                return CollUtils.emptyList();
            }
            //
            @Override
            public void deductStock(List<OrderDetailDTO> orderDetailDTOS) {
                // 库存扣减业务需要触发事务回滚，查询失败，抛出异常
                throw new BizIllegalException(cause);
            }

            @Override
            public void recoverStore(List<OrderDetailDTO> orderDetails) {
                throw new BizIllegalException(cause);
            }
        };
    }
}
