package com.cavus.delivery_food.product.service;


import com.cavus.delivery_food.category.entity.Category;
import com.cavus.delivery_food.category.service.CategoryService;
import com.cavus.delivery_food.product.dto.ProductRequest;
import com.cavus.delivery_food.product.dto.ProductResponse;
import com.cavus.delivery_food.product.entity.Product;
import com.cavus.delivery_food.product.mapper.ProductMapper;
import com.cavus.delivery_food.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
///  `@Transactional` bir metot içindeki tüm DB işlemlerinin tek bir transaction'da (atomik) yapılmasını sağlar — biri başarısız olursa hepsi geri alınır.
public class  ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, CategoryService categoryService){
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryService = categoryService;
    }

    /// `@Transactional(readOnly = true)` ne demek?** Sadece okuma yapan metotlarda (findAll, findById) performans için "bu metot veri değiştirmiyor" bilgisini verir.

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(){
        List<Product> allProducts = productRepository.findAll();
        return  productMapper.toProductResponseList(allProducts);
    }


    /// Burada exception olduğunda springboot RestControllerAdvice annotation arar  (Bu exception kimde handle ediliyor?)
    /// Hata varsa fırlatırım, nasıl cevap verileceğini umursamam
    /// ProductExceptionHandler'a bakıyor RestControllerAdvice orada var
    /// @ExceptionHandler(ProductNotFoundException.class) Bu tip hata olursa BEN DEVREYE GİRERİM ve handler classında olan formatta response döner
    @Transactional(readOnly = true)
    public ProductResponse findById(UUID uuid){
        Product product = productRepository.findById(uuid)
                .orElseThrow(() -> new ProductNotFoundException(uuid));

      return productMapper.toProductResponse(product);

    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product entity = productMapper.toEntity(request);
        /// Produuct'a category ekleme
        if(request.getCategoryId() != null){
            Category category = categoryService.getEntityById(request.getCategoryId());
            entity.setCategory(category);
        }
        Product savedProduct = productRepository.save(entity);
        return productMapper.toProductResponse(savedProduct);
    }

    /// Ürünü kategoriye atama
    @Transactional
    public ProductResponse assignCategory(UUID productId,UUID categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Category category = categoryService.getEntityById(categoryId);

        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        return productMapper.toProductResponse(savedProduct);
    }

    /// Toplu ürünleri kategoriye atama
    @Transactional
    public List<ProductResponse> assignCategoryBulk(List<UUID> productIds, UUID categoryId){
        if(productIds == null || productIds.isEmpty()){
            throw new IllegalArgumentException("Ürün listesi boş olamaz");
        }
        categoryService.getEntityById(categoryId);
        List<Product> products = productRepository.findAllById(productIds);

        if(products.isEmpty() || products.size() != productIds.size()){
            throw new IllegalArgumentException("Belirtilen ürünler bulunamadı");
        }
        for(Product product : products){
            if(product.getCategory() != null){
                throw new IllegalArgumentException("Bu ürün zaten bir kategoriye atanmış, ürün ismi: " + product.getName()+ "id: " + product.getId());
            }
            product.setCategory(categoryService.getEntityById(categoryId));
        }
        List<Product> savedProducts = productRepository.saveAll(products);
        return productMapper.toProductResponseList(savedProducts);
    }



    /// Kategoriye göre ürün listeleme
    @Transactional(readOnly = true)
    public List<ProductResponse> findProductsFromCategory(UUID categoryId){
        categoryService.getEntityById(categoryId);

        List<Product> products = productRepository.findByCategoryId(categoryId);
        return productMapper.toProductResponseList(products);
    }

    @Transactional
    public List<ProductResponse> createBulk(List<ProductRequest> requests) {
        List<Product> productList = productMapper.toProductList(requests);
        List<Product> products = productRepository.saveAll(productList);
        return productMapper.toProductResponseList(products);
    }

    @Transactional
    public ProductResponse update(UUID uuid, ProductRequest request) {

        Product product = productRepository.findById(uuid)
                .orElseThrow(() -> new ProductNotFoundException(uuid));

        productMapper.updateProductFromRequest(request, product);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toProductResponse(updatedProduct);
    }
    @Transactional
    public void delete(UUID uuid) {
        Product product = productRepository.findById(uuid)
                .orElseThrow(() -> new ProductNotFoundException(uuid));

        productRepository.delete(product);
    }
}

