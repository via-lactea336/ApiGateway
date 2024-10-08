package com.miapp.apigateway.filter;

import com.miapp.apigateway.util.AuthUtil;
import com.miapp.apigateway.validator.RouteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GatewayFilter {

    @Autowired
    private RouteValidator routeValidator;

    @Autowired
    private AuthUtil authUtil;

    @Value("${authentication.enabled:true}")
    private boolean authEnabled;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!authEnabled) {
            System.out.println("Authentication is disabled. To enable it, make \"authentication.enabled\" property as true");
            return chain.filter(exchange);
        }

        String token = "";
        ServerHttpRequest request = exchange.getRequest();

        if (routeValidator.isSecured.test(request)) {
            System.out.println("validating authentication token");
            if (isMissingCredentials(request)) {
                System.out.println("in error");
                return this.onError(exchange, "Credentials missing", HttpStatus.UNAUTHORIZED);
            }

            // Validación del token
            if (request.getHeaders().containsKey("username") && request.getHeaders().containsKey("role")) {
                token = authUtil.getToken(request.getHeaders().get("username").toString(), request.getHeaders().get("role").toString());
            } else {
                token = request.getHeaders().get("Authorization").toString().split(" ")[1];
            }

            if (authUtil.isInvalid(token)) {
                return this.onError(exchange, "Auth header invalid", HttpStatus.UNAUTHORIZED);
            }

            System.out.println("Authentication is successful");
        }

        return chain.filter(exchange);
    }

    private boolean isMissingCredentials(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("username") || !request.getHeaders().containsKey("role");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMsg, HttpStatus status) {
        // Manejo del error, puedes personalizarlo según tus necesidades
        return exchange.getResponse().setComplete();
    }
}