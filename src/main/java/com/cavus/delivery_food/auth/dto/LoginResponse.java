package com.cavus.delivery_food.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;
    private String tokenType;

}
