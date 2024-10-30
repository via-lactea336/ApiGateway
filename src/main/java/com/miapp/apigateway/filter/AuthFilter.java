package com.miapp.apigateway.filter;

import com.miapp.apigateway.util.JwtTokenProviderT;
import com.miapp.apigateway.validator.RouteValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Autowired
    private RouteValidator routeValidator;



    @Value("${authentication.enabled:true}")
    private boolean authEnabled;

    @Autowired
    private JwtTokenProviderT jwtTokenProvider; // Añade JwtTokenProvider para la validación del token

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!authEnabled) {
            logger.info("Authentication is disabled.");
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        logger.info("Request Path: {}", request.getURI().getPath());

        if (routeValidator.isSecured.test(request)) {
            logger.info("Validating authentication token");

            // Obtener el token JWT de la cabecera Authorization
            String token = resolveToken(request);
            logger.info("Token: {}", token);
            if (token != null) {
                // Validar el token
                if (!jwtTokenProvider.validateToken(token)) {
                    return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                }

                // Extraer información del token
                String username = jwtTokenProvider.getUsernameFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);
                logger.info("Authenticated user: {}, Role: {}", username, role);

            } else {
                return onError(exchange, "Authorization header is missing or invalid", HttpStatus.UNAUTHORIZED);
            }

            logger.info("Authentication is successful");
        } else {
            logger.info("Route is not secured, proceeding without authentication");
        }

        return chain.filter(exchange);
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        return bearerToken != null && bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMsg, HttpStatus status) {
        logger.error("Error: {}, Status: {}", errorMsg, status);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
