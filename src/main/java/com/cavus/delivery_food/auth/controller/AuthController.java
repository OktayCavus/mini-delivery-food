package com.cavus.delivery_food.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cavus.delivery_food.auth.dto.LoginRequest;
import com.cavus.delivery_food.auth.dto.LoginResponse;
import com.cavus.delivery_food.auth.dto.RefreshTokenRequest;
import com.cavus.delivery_food.auth.dto.RefreshTokenResponse;
import com.cavus.delivery_food.auth.dto.RegisterRequest;
import com.cavus.delivery_food.auth.dto.RegisterResponse;
import com.cavus.delivery_food.auth.service.AuthService;
import com.cavus.delivery_food.common.response.BaseResponse;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<RegisterResponse>> register(
           @Valid @RequestBody RegisterRequest request) {
    
        RegisterResponse response = authService.register(request);
    
        return ResponseEntity.ok(
                BaseResponse.success(200, "Başarıyla oluşturuldu", response)
        );
    }
    
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(BaseResponse.success(200, "Başarıyla giriş yapıldı", response));
    }
    
    @PostMapping("/refresh")
public ResponseEntity<BaseResponse<RefreshTokenResponse>> refresh(
        @Valid @RequestBody RefreshTokenRequest request) {

    RefreshTokenResponse response = authService.refresh(request);

    return ResponseEntity.ok(
        BaseResponse.success(200, "Token yenilendi", response)
    );
}

@PostMapping("/logout")
public ResponseEntity<BaseResponse<Void>> logout(
        @Valid @RequestBody RefreshTokenRequest request) {

    authService.logout(request);

    return ResponseEntity.ok(
        BaseResponse.success(200, "Çıkış yapıldı", null)
    );
}
}
