package com.minibanking.api_gateway.controller;

import com.minibanking.api_gateway.dto.LoginRequest;
import com.minibanking.api_gateway.dto.TokenResponse;
import com.minibanking.api_gateway.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(
            @Valid @RequestBody LoginRequest request
    ) {

        if (!"admin".equals(request.getUsername())
                || !"admin123".equals(request.getPassword())) {

            return ResponseEntity
                    .status(401)
                    .body("Invalid username or password");
        }

        String token =
                jwtService.generateToken(request.getUsername());

        return ResponseEntity.ok(
                new TokenResponse(
                        token,
                        "Bearer",
                        expiration
                )
        );
    }
}