package com.cavus.delivery_food.product.repository;

import com.cavus.delivery_food.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByCategoryId(UUID categoryId);
    /// Spring findByCategoryId bu ifadeyi parçalıyor findBy Category Id olarak
    /// Bu şunu temsil ediyor findBy -> sorguyu başlatıyor
    /// Category -> Product tablosunda category alanına gir
    /// Id -> Category entity'sinin `id` alanına bak
    
    List<Product> findByOutletId(UUID outletId);

    List<Product> findByOutletIdAndCategoryId(UUID outletId, UUID categoryId);
}
