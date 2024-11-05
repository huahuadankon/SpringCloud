package com.hmall.api.client.fallback;

import com.hmall.api.client.UserClient;
import com.hmall.common.exception.BizIllegalException;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author liuyichen
 * @version 1.0
 */
public class UserClientFallBack implements FallbackFactory<UserClient> {
    public UserClient create(Throwable throwable) {
        return new UserClient() {

            @Override
            public void deductMoney(String pw, Integer amount) {
                //更新状态失败
                throw new BizIllegalException(throwable);
            }
        };

        }
    }

