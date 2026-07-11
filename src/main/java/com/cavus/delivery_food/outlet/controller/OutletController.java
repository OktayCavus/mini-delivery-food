package com.cavus.delivery_food.outlet.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cavus.delivery_food.category.dto.CategoryRequest;
import com.cavus.delivery_food.category.dto.CategoryResponse;
import com.cavus.delivery_food.category.service.CategoryService;
import com.cavus.delivery_food.common.response.BaseResponse;
import com.cavus.delivery_food.outlet.dto.OutletRequest;
import com.cavus.delivery_food.outlet.dto.OutletResponse;
import com.cavus.delivery_food.outlet.service.OutletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/outlets")
@RequiredArgsConstructor
public class OutletController {

    private final OutletService outletService;
    private final CategoryService categoryService;

   
    // create
    @PostMapping
    public ResponseEntity<BaseResponse<OutletResponse>> create(@Valid @RequestBody OutletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.value(), "Outlet başarıyla oluşturuldu", outletService.create(request)));
    }

    // getAll
    @GetMapping
    public ResponseEntity<BaseResponse<List<OutletResponse>>> getAll() {
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK.value(), "Outlet listesi başarıyla getirildi", outletService.findAllByActive()));
    }


    // getById

     @GetMapping("/{id}")
     public ResponseEntity<BaseResponse<OutletResponse>> getById(@PathVariable UUID id) {
         return ResponseEntity.ok(BaseResponse.success(200, "Outlet getirildi",
                 outletService.findById(id)));
     }
    
     // update
     @PutMapping("/{id}")
     public ResponseEntity<BaseResponse<OutletResponse>> update(@PathVariable UUID id,
             @Valid @RequestBody OutletRequest request) {
         return ResponseEntity.ok(
                 BaseResponse.success(HttpStatus.OK.value(), "Outlet güncellendi", outletService.update(id, request)));
     }
    
    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse<List<OutletResponse>>> createBulk(
            @Valid @RequestBody List<@Valid OutletRequest> requests) {
        List<OutletResponse> created = outletService.createBulk(requests);
        return ResponseEntity.status(201)
                .body(BaseResponse.success(201, "Outlet'ler oluşturuldu", created));
    }

    @GetMapping("/{outletId}/categories")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getOutletCategories(@PathVariable UUID outletId) {
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK.value(), "Kategori listesi başarıyla getirildi",
                categoryService.findAllByOutletId(outletId)));
    }
    
    @PostMapping("/{outletId}/categories")
    public ResponseEntity<BaseResponse<CategoryResponse>> createCategoryForOutlet(@PathVariable UUID outletId, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.value(), "Kategori başarıyla oluşturuldu",
                        categoryService.createCategoryForOutlet(outletId, request)));
    }

  

   
}
