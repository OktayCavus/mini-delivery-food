package com.cavus.delivery_food.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ürün filtreleme parametreleri", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
public class ProductFilterRequest {


    @Schema(description = "Ürün adı (partial, case-insensitive)", example = "pizza", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 255, message = "Arama terimi en fazla 255 karakter olabilir")
    private String name;

    @Schema(description = "Açıklama içinde arama (partial, case-insensitive)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500)
    private String description;

    @Schema(description = "Minimum fiyat", example = "100.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DecimalMin(value = "0.0", message = "Minimum fiyat 0'dan küçük olamaz")
    private BigDecimal minPrice;

    @Schema(description = "Maksimum fiyat", example = "500.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DecimalMin(value = "0.0", message = "Maksimum fiyat 0'dan küçük olamaz")
    private BigDecimal maxPrice;

    @Schema(description = "Minimum stok", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer minStock;

    @Schema(description = "Maksimum stok", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer maxStock;

    @Schema(description = "Aktiflik durumu", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;

    @Schema(description = "Kategori ID filtresi", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID categoryId;

    @Schema(description = "Outlet ID filtresi", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID outletId;

    @Schema(description = "Genel arama terimi (name ve description'da arar)", example = "iphone", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String search;
}
