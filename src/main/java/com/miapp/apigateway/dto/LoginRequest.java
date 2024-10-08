package com.miapp.apigateway.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
    // Getters y Setters
    private String email;
    private String contrasena;

    public LoginRequest(String email, String contrasena) {
        this.email = email;
        this.contrasena = contrasena;
    }

}