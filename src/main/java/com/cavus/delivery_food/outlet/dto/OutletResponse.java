package com.cavus.delivery_food.outlet.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutletResponse {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private Boolean active;
    
}
