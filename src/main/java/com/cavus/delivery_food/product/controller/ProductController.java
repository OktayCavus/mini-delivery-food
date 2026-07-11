package com.cavus.delivery_food.product.controller;


import com.cavus.delivery_food.common.response.BaseResponse;
import com.cavus.delivery_food.product.dto.ProductRequest;
import com.cavus.delivery_food.product.dto.ProductResponse;
import com.cavus.delivery_food.product.mapper.ProductMapper;
import com.cavus.delivery_food.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products")
@RequiredArgsConstructor
public class ProductController {

    final private ProductService productService;

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

    @PostMapping("/bulk")
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


    /// Ürünü kategoriye atama
    @PutMapping("/{productId}/category/{categoryId}")
    @Operation(summary = "Ürünü kategoriye ata")
    @ApiResponse(responseCode = "200", description = "Ürün kategoriye başarıyla atandı")
    @ApiResponse(responseCode = "404", description = "Ürün veya kategori bulunamadı")
    public ResponseEntity<BaseResponse<ProductResponse>> assignCategory(
            @Parameter(description = "Ürün ID değeri") @PathVariable UUID productId,
            @Parameter(description = "Kategori ID değeri") @PathVariable UUID categoryId) {

        ProductResponse productResponse = productService.assignCategory(productId, categoryId);

        return ResponseEntity.ok(
                BaseResponse.success(200, "Ürün kategoriye başarıyla atandı", productResponse)
        );
    }

    /// Toplu ürünleri kategoriye atama
    @PutMapping("/bulk/category/{categoryId}")
    @Operation(summary = "Toplu olarak Ürünleri kategoriye ata")
    @ApiResponse(responseCode = "200", description = "Ürünler kategoriye başarıyla atandı")
    @ApiResponse(responseCode = "404", description = "Ürünler veya kategori bulunamadı")
    public ResponseEntity<BaseResponse<List<ProductResponse>>> assignCategoryBulk(@PathVariable UUID categoryId ,@RequestBody List<UUID> ids){

        List<ProductResponse> products = productService.assignCategoryBulk(ids, categoryId);

        return ResponseEntity.ok(
                BaseResponse.success(200, "Ürünler kategoriye başarıyla atandı", products)
        );
    }

    /// Kategoriye göre ürün listeleme
    @GetMapping("/by-category/{categoryId}")
    @Operation(summary = "Kategoriye ait ürünleri listele")
    @ApiResponse(responseCode = "200", description = "Kategoriye ait ürünler başarıyla listelendi")
    @ApiResponse(responseCode = "404", description = "Kategori bulunamadı")
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getProductsByCategory(
            @Parameter(description = "Kategori ID değeri") @PathVariable UUID categoryId) {

        List<ProductResponse> products = productService.findProductsFromCategory(categoryId);

        return ResponseEntity.ok(
                BaseResponse.success(200, "Kategoriye ait ürünler listelendi", products));
    }
    
    @GetMapping("/by-outlet/{outletId}")
@Operation(summary = "Outlet menüsündeki ürünleri listele")
public ResponseEntity<BaseResponse<List<ProductResponse>>> getProductsByOutlet(
        @PathVariable UUID outletId) {
    List<ProductResponse> products = productService.findByOutletId(outletId);
    return ResponseEntity.ok(
            BaseResponse.success(200, "Outlet ürünleri listelendi", products));
}
}
