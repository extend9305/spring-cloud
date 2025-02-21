package com.example.springgateway.filter;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory <AuthorizationHeaderFilter.Config> {
    Environment environment;

    public AuthorizationHeaderFilter(Environment environment) {
        super(AuthorizationHeaderFilter.Config.class);
        this.environment = environment;
    }

    public static class Config {

    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return orError(exchange,"No authentication header", HttpStatus.UNAUTHORIZED);
            }
            String autorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = autorizationHeader.replace("Bearer", "");

            if(!isJwtValid(jwt)){
                return orError(exchange,"Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        });
    }
    // Mono, Flux -> Spring WebFlux
    private Mono<Void> orError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(err);
        return response.setComplete();
    }

    private boolean isJwtValid(String jwt) {
        boolean isValid = true;

        String subject = null;
        try{
            log.info("token.secret : " + environment.getProperty("token.secret"));
            subject = Jwts.parser().setSigningKey(environment.getProperty("token.secret"))
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();
        }catch (Exception e){
            isValid = false;
        }

        if (subject == null || subject.isEmpty()) {
            isValid = false;
        }
        return isValid;
    }
}
