package com.cavus.delivery_food.product.service;


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

    public ProductService(ProductRepository productRepository, ProductMapper productMapper){
        this.productRepository = productRepository;
        this.productMapper = productMapper;
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
        Product savedProduct = productRepository.save(entity);
        return productMapper.toProductResponse(savedProduct);

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

