package com.cavus.delivery_food.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private Integer stock;

    private String unit;

    private Boolean active;

    private String categoryId;

    private String categoryName;

    private String outletId;

    private String outletName;
}
