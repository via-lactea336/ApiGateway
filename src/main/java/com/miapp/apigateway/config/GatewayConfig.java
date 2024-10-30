package com.miapp.apigateway.config;

import com.miapp.apigateway.filter.AuthFilter;
import com.miapp.apigateway.filter.RequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class GatewayConfig {

    @Autowired
    private RequestFilter requestFilter;

    @Autowired
    private AuthFilter authFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // login
                .route(id -> id.path("/api/auth/login")
                        .uri("http://localhost:9191"))
                //register
                .route(id -> id.path("/api/auth/register")
                        .uri("http://localhost:9191"))
                // Microservicio de roles
                .route(id -> id.path("/api/roles/**")
                        .filters(f -> f.filters(requestFilter, authFilter))
                        .uri("http://localhost:9191"))

                // Microservicio de calificaciones
                .route(id -> id.path("/api/calificaciones/**")
                        .filters(f -> f.filters(requestFilter, authFilter))
                        .uri("http://localhost:8383"))

                .route(id -> id.path("/api/calificaciones-detalle/**")
                        .filters(f -> f.filters(requestFilter, authFilter))
                        .uri("http://localhost:8383"))

                .route(id -> id.path("/api/categorias/**")
                        .filters(f -> f.filters(requestFilter, authFilter))
                        .uri("http://localhost:8383"))

                // Endpoint para estados de reportes
                .route(id -> id.path("/api/estados/**")
                        .filters(f -> f.filters(requestFilter, authFilter))
                        .uri("http://localhost:8383"))
                .route(id -> id.path("/api/tipos-de-precio/**")
                        .filters(f -> f.filters(requestFilter, authFilter))
                        .uri("http://localhost:8383"))

                .build();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/**").permitAll() // Permitir acceso sin autenticación a todas las rutas
                        .anyExchange().permitAll()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }

}
