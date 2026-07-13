package com.cavus.delivery_food.outlet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Outlet filtreleme parametreleri", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
public class OutletFilterRequest {

    @Schema(description = "Outlet adı (partial, case-insensitive)",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100, message = "Arama terimi en fazla 100 karakter olabilir")
    private String name;

    @Schema(description = "Adres içinde arama (partial, case-insensitive)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500)
    private String address;

    @Schema(description = "Telefon numarası (partial)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 15)
    private String phone;

    @Schema(description = "E-posta içinde arama (partial, case-insensitive)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100)
    private String email;

    @Schema(description = "Aktiflik durumu", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;

    @Schema(description = "Genel arama (name, address, phone, email alanlarında arar)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 255, message = "Arama terimi en fazla 255 karakter olabilir")
    private String search;
}
