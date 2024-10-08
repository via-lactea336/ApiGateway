package com.miapp.apigateway.util;

import java.util.Date;
import com.miapp.apigateway.dto.LoginRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    private RestTemplate restTemplate;

    public String getToken(String userName, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("userName", userName);
        headers.set("role", role);
        HttpEntity<LoginRequest> request = new HttpEntity<>(
                new LoginRequest("bryan", "admin"), headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8088/login", HttpMethod.POST, request, String.class);
        System.out.println("token:" + response.getBody());
        return response.getBody();
    }
    public boolean isInvalid(String token) {
        try {
            // Verificar el token y obtener los reclamos
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            // Verificar la fecha de expiración del token
            return claims.getExpiration().before(new Date());

        } catch (Exception e) {
            // Si hay una excepción, el token es inválido
            return true;
        }
    }
}