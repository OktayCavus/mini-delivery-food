package com.cavus.delivery_food.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cavus.delivery_food.auth.dto.LoginRequest;
import com.cavus.delivery_food.auth.dto.LoginResponse;
import com.cavus.delivery_food.auth.dto.RegisterRequest;
import com.cavus.delivery_food.auth.dto.RegisterResponse;
import com.cavus.delivery_food.auth.service.AuthService;
import com.cavus.delivery_food.common.response.BaseResponse;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<RegisterResponse>> register(
            @RequestBody RegisterRequest request) {
    
        RegisterResponse response = authService.register(request);
    
        return ResponseEntity.ok(
                BaseResponse.success(200, "Başarıyla oluşturuldu", response)
        );
    }
    
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @RequestBody LoginRequest request) {
    
        LoginResponse response = authService.login(request);
    
        return ResponseEntity.ok(BaseResponse.success(200, "Başarıyla giriş yapıldı", response));
    }
}
