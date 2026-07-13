package com.cavus.delivery_food.category.controller;


import static com.cavus.delivery_food.auth.entity.Permissions.CATEGORY_CREATE;
import static com.cavus.delivery_food.auth.entity.Permissions.CATEGORY_READ;

import com.cavus.delivery_food.category.dto.CategoryRequest;
import com.cavus.delivery_food.category.dto.CategoryResponse;
import com.cavus.delivery_food.category.service.CategoryService;
import com.cavus.delivery_food.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PreAuthorize("hasAuthority('" + CATEGORY_CREATE + "')")
    @PostMapping
    public ResponseEntity<BaseResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest categoryRequest){
        CategoryResponse createdCategory = categoryService.createCategoryForOutlet(categoryRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(createdCategory.getId()).toUri();

        return ResponseEntity.created(location).body(BaseResponse.success(201, "Kategori Başarıyla oluşturuldu", createdCategory));
    }

    @PreAuthorize("hasAuthority('" + CATEGORY_READ + "')")
    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.findAll();
        return ResponseEntity.ok(BaseResponse.success(200, "Kategoriler başarıyla listelendi", categories));
    }

    @PreAuthorize("hasAuthority('" + CATEGORY_CREATE + "')")
    @PostMapping("/bulk")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> createBulk(
            @Valid @RequestBody List<@Valid CategoryRequest> categoryRequests
    ) {
        List<CategoryResponse> createdCategories = categoryService.createBulk(categoryRequests);

        return ResponseEntity.status(201)
                .body(BaseResponse.success(201, "Kategoriler başarıyla oluşturuldu", createdCategories));
    }
}
