package com.miapp.apigateway.config;

import com.miapp.apigateway.filter.AuthFilter;
import com.miapp.apigateway.filter.RequestFilter;
import com.miapp.sistemasdistribuidos.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    @Autowired
    RequestFilter requestFilter;

    @Autowired
    AuthFilter authFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        // adding 2 routes to first microservice as we need to log request body if method is POST
        return builder.routes()
                .route(id -> id.path("/first")
                        .and().method(HttpMethod.POST)
                        .and().readBody(Usuario.class, s -> true)
                        .filters(f -> f.filters(requestFilter, authFilter))
                        .uri("http://localhost:8081"))
                .route(id -> id.path("/first")
                        .and().method(HttpMethod.GET)
                        .uri("http://localhost:8081"))
                .route(id -> id.path("/second")
                        .and().method(HttpMethod.GET)
                        .uri("http://localhost:8082"))
                .route(id -> id.path("/login")
                        .uri("http://localhost:8080/login"))
                .build();
    }
}
