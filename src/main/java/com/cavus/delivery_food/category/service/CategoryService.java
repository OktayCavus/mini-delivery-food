package com.cavus.delivery_food.category.service;

import com.cavus.delivery_food.category.dto.CategoryRequest;
import com.cavus.delivery_food.category.dto.CategoryResponse;
import com.cavus.delivery_food.category.entity.Category;
import com.cavus.delivery_food.category.mapper.CategoryMapper;
import com.cavus.delivery_food.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

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

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
