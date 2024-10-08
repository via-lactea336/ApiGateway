package com.miapp.apigateway.validator;

import com.miapp.apigateway.util.TokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    @Autowired
    private TokenUtil tokenUtil;

    public static final List<String> unprotectedURLs = List.of("/login");

    public Predicate<ServerHttpRequest> isSecured = request -> unprotectedURLs.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    private boolean isCredMissing(ServerHttpRequest request) {
        return !(request.getHeaders().containsKey("userName") && request.getHeaders().containsKey("role") && request.getHeaders().containsKey("Authorization"));
    }

    private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        Claims claims = tokenUtil.getAllClaimsFromToken(token);
        exchange.getRequest().mutate().header("userName", String.valueOf(claims.get("userName"))).header("role", String.valueOf(claims.get("role"))).build();
    }
}
