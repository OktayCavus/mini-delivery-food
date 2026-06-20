package com.cavus.delivery_food.product.controller;


import com.cavus.delivery_food.common.response.BaseResponse;
import com.cavus.delivery_food.product.dto.ProductRequest;
import com.cavus.delivery_food.product.dto.ProductResponse;
import com.cavus.delivery_food.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Ürün CRUD işlemleri")
public class ProductController {

    final private ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Tüm ürünleri listele")
    @ApiResponse(responseCode = "200", description = "Ürünler başarıyla listelendi")
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getAllProducts(){
       List<ProductResponse> products = productService.findAll();
       return ResponseEntity.ok(BaseResponse.success(200, "Ürünler başarıyla listelendi", products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID'ye göre ürün getir")
    @ApiResponse(responseCode = "200", description = "Ürün bulundu")
    @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
    public ResponseEntity<BaseResponse<ProductResponse>> getProductById(
            @Parameter(description = "Ürün ID değeri") @PathVariable("id") UUID uuid) {
       ProductResponse product = productService.findById(uuid);
       return ResponseEntity.ok(BaseResponse.success(200, "Ürün başarıyla getirildi", product));
    }

    /// @Valid ProductRequest içindeki validation annotation'larını çalıştırır.
    @PostMapping
    @Operation(summary = "Yeni ürün oluştur")
    @ApiResponse(responseCode = "201", description = "Ürün başarıyla oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Geçersiz ürün verisi")
    public ResponseEntity<BaseResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request){
        ProductResponse createdProduct = productService.create(request);

        /// Bu parça client için faydalıymış sebebi direkt oluşan url'i döndürüyor bu şekilde client direkt içeriğe gidebilir

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(BaseResponse.success(201, "Ürün başarıyla oluşturuldu", createdProduct));
    }

    @PostMapping("/create-bulk")
    @Operation(summary = "Yeni ürünleri toplu oluştur")
    @ApiResponse(responseCode = "201", description = "Ürünler başarıyla oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Geçersiz ürün verisi")
    public ResponseEntity<BaseResponse<List<ProductResponse>>> createBulk(@Valid @RequestBody List<ProductRequest> request){
        List<ProductResponse> bulkedProducts = productService.createBulk(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(201, "Ürünler başarıyla oluşturuldu", bulkedProducts));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Ürün güncelle")
    @ApiResponse(responseCode = "200", description = "Ürün başarıyla güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz ürün verisi")
    @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
    public ResponseEntity<BaseResponse<ProductResponse>> update(
            @Parameter(description = "Ürün ID değeri") @PathVariable("id") UUID uuid,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse updatedProduct = productService.update(uuid, request);
        return ResponseEntity.ok(BaseResponse.success(200, "Ürün başarıyla güncellendi", updatedProduct));

    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Ürün sil")
    @ApiResponse(responseCode = "200", description = "Ürün başarıyla silindi")
    @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
    public ResponseEntity<BaseResponse<Void>> delete(
            @Parameter(description = "Ürün ID değeri") @PathVariable("id") UUID uuid) {
        productService.delete(uuid);
        return ResponseEntity.ok(BaseResponse.success(200, "Ürün başarıyla silindi", null));
    }


}
