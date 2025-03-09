package com.example.springgateway.filter;

import jdk.jfr.DataAmount;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

    public GlobalFilter() {super(GlobalFilter.Config.class);}

    private static final Logger log = LoggerFactory.getLogger(GlobalFilter.class);

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Global filter baseMessage : {}", config.getBaseMessage());

            if (config.isPreLogger()){
                log.info("Global filter Start: request id -> {}", request.getId());
            }
            log.info("Global filter Start: request id -> {}", request.getHeaders());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()){
                    log.info("Global filter End: response code -> {}", response.getStatusCode());
                }
            }));

        });
    }
    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }

}
