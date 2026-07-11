package com.cavus.delivery_food.product.repository;

import com.cavus.delivery_food.product.entity.Product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ProductRepository extends JpaRepository<Product, UUID> {


    /// ! N + 1 problemi diye bir problem var. Birbiri ile ilişkili olan entity'leri birlikte getirir.
    /// ! yani şöyle burada findAll dediğimde query Select * From products sonra ürünler başına category'leri çekmek için 
    /// ! her ürün için SELECT * FROM category WHERE id = ?; sorgusu çalışır bu da  N + 1 problemi olur.
    ///! Engellemek için EntityGraph annotation'u kullanılır.
    /// ! bu annotation sayesinde tek sorguda 
    /* 
    SELECT p.*,
       c.*,
       o.*
    FROM product p
    LEFT JOIN category c
       ON p.category_id = c.id
    LEFT JOIN outlet o
       ON p.outlet_id = o.id
    */
   
    /// ! bu şekilde sorgu çalıştırılır. attributePaths bize diyor ki Product yüklenirken category ve outlet'i de yükle.
    /// ! bu sayede N + 1 problemi çözülür.
    /// ! bu annotation 
    /*
    @EntityGraph(attributePaths = {
    "category",
    "outlet.address"
    })
    */
   /// ! Bu şekilde de olabilirdi.
    @EntityGraph(attributePaths = {"category", "outlet"})
    List<Product> findAll();

    @EntityGraph(attributePaths = {"category", "outlet"})
    List<Product> findByCategoryId(UUID categoryId);
    /// Spring findByCategoryId bu ifadeyi parçalıyor findBy Category Id olarak
    /// Bu şunu temsil ediyor findBy -> sorguyu başlatıyor
    /// Category -> Product tablosunda category alanına gir
    /// Id -> Category entity'sinin `id` alanına bak
    
    @EntityGraph(attributePaths = {"category", "outlet"})
    List<Product> findByOutletId(UUID outletId);

    @EntityGraph(attributePaths = {"category", "outlet"})
    List<Product> findByOutletIdAndCategoryId(UUID outletId, UUID categoryId);
}
