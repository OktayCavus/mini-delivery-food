package com.cavus.delivery_food.outlet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutletRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(min = 3, max = 500, message = "Address must be between 3 and 500 characters")
    private String address;

    @Size(min = 10, max = 15, message = "Phone must be between 10 and 15 characters")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    private Boolean active = true;
 
}
