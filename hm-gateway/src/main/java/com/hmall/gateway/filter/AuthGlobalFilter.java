package com.hmall.gateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.util.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author liuyichen
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthProperties.class)
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final JwtTool jwtTool;

    private final AuthProperties authProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //判断请求是否需要拦截
        val request = exchange.getRequest();
        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange);
        }

        //获取请求头
        List<String> tokens = request.getHeaders().get("Authorization");
        String token = null;
        if(tokens != null && tokens.size() > 0){
             token = tokens.get(0);
        }

        Long userId =null;
        //解析token
        try {
             userId = jwtTool.parseToken(token);
        }catch (Exception e){
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(401);
            return response.setComplete();
        }
        String userinfo = userId.toString();



        //用户id传递到微服务
        ServerWebExchange webExchange = exchange.mutate().
                request(builder -> builder.header("user-info", userinfo)).build();

        return chain.filter(webExchange);
    }

    private Boolean isExclude(String path){
        for (String pathPattern: authProperties.getExcludePaths()){
            if(antPathMatcher.match(pathPattern, path)){
                return true;
            }
        }
        return false;
    }



    @Override
    public int getOrder() {
        return 0;
    }
}
