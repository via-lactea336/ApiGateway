package com.miapp.apigateway.filter;


import com.miapp.sistemasdistribuidos.entity.Usuario;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Object body = exchange.getAttribute("cachedRequestBodyObject");
        System.out.println("in request filter");

        if (body instanceof Usuario) {
            System.out.println("body: " + (Usuario) body);
        }

        return chain.filter(exchange);
    }
}