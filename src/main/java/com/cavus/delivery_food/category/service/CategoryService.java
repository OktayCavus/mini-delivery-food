package com.cavus.delivery_food.category.service;

import com.cavus.delivery_food.category.dto.CategoryRequest;
import com.cavus.delivery_food.category.dto.CategoryResponse;
import com.cavus.delivery_food.category.entity.Category;
import com.cavus.delivery_food.category.mapper.CategoryMapper;
import com.cavus.delivery_food.category.repository.CategoryRepository;
import com.cavus.delivery_food.outlet.entity.Outlet;
import com.cavus.delivery_food.outlet.service.OutletService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final OutletService outletService;

  

    public CategoryResponse create(CategoryRequest request) {
        String normalizedName = normalizeName(request.getName());

        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Bu kategori adı zaten kullanılıyor: " + request.getName());
        }

        Category entity = categoryMapper.toEntity(request);
        entity.setName(normalizedName);
        Category savedCategory = categoryRepository.save(entity);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toCategoryResponseList(categories);
    }

    /// Burası response yerine Entity dönüyor çünkü `ProductService`, ürün ile kategori ilişkisi kurarken gerçek `Category` entity'sine ihtiyaç duyar
    @Transactional(readOnly = true)
    public Category getEntityById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    public List<CategoryResponse> createBulk(List<CategoryRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Kategori listesi boş olamaz");
        }

        Set<String> requestNames = new HashSet<>();

        for (CategoryRequest request : requests) {
            String name = normalizeName(request.getName());

            if (!requestNames.add(name)) {
                throw new IllegalArgumentException("Liste içinde tekrar eden kategori adı var: " + request.getName());
            }

            if (categoryRepository.existsByNameIgnoreCase(name)) {
                throw new IllegalArgumentException("Bu kategori adı zaten kullanılıyor: " + request.getName());
            }
        }

        List<Category> categories = requests.stream()
                .map(request -> {
                    Category category = categoryMapper.toEntity(request);
                    category.setName(normalizeName(request.getName()));
                    return category;
                })
                .toList();

        List<Category> savedCategories = categoryRepository.saveAll(categories);

        return categoryMapper.toCategoryResponseList(savedCategories);
    }

    public List<CategoryResponse> findAllByOutletId(UUID outletId) {
          outletService.getEntityById(outletId);
    return categoryMapper.toCategoryResponseList(categoryRepository.findAllByOutletIdAndActiveTrue(outletId));
    }

    public CategoryResponse createCategoryForOutlet(UUID outletId, CategoryRequest request) {
        Outlet outlet = outletService.getEntityById(outletId);

        String normalizedName = normalizeName(request.getName());

        if (categoryRepository.existsByNameIgnoreCaseAndOutletId(normalizedName, outletId)) {
            throw new CategoryExistException(normalizedName);
        }

        Category category = categoryMapper.toEntity(request);

        category.setName(normalizedName);

        category.setOutlet(outlet);

       return categoryMapper.toCategoryResponse( categoryRepository.save(category));

        
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
