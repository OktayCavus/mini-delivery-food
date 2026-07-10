package com.cavus.delivery_food.product.dto;


import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Ürün adı boş olamaz")
    private String name;

    @Size(max = 500)
    private String description;

    // ! inclusive = false bu değer kesinlikle 0'dan büyük olmalıdır demek
    @NotNull(message = "Fiyat zorunludur")
    @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat 0'dan büyük olmalı")
    private BigDecimal price;

    private String imageUrl;

    @Min(value = 0, message = "Stok 0'dan küçük olamaz")
    private Integer stock = 0;

    @Size(max = 20)
    private String unit;

    private Boolean active = true;

    private UUID categoryId;

    @NotNull(message = "Outlet ID zorunludur")
    private UUID outletId;
}
