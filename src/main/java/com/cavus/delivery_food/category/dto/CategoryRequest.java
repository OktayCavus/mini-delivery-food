package com.cavus.delivery_food.category.dto;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Kategori ismi boş bırakılamaz")
    @Size(max = 100, message = "Kategori adı en fazla 100 karakter olabilir")
    private String name;

    @Size(max = 500, message = "Kategori açıklaması en fazla 500 karakter olabilir")
    private String description;


    private Boolean active = true;

}
