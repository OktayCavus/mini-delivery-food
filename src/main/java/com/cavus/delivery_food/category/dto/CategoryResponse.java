package com.cavus.delivery_food.category.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private Boolean active;
    private String outletId;
    private String outletName;

    /// Product listesi response'da yok çünkü kategori listesi çekerken her kategoriyle birlikte tüm ürünleri de döndürmek performans açısından risklidir.
}
