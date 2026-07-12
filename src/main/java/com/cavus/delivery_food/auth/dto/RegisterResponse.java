package com.cavus.delivery_food.auth.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {

    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    
}
