package com.cavus.delivery_food.product.service;


import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID id) {
        /// buradaki super exception içerisine mesajı koyar  ProductExceptionHandler'daki ex.getMessage o mesajı okur gibi çalışıyor
        super("Ürün bulunamadı, id: " + id);
    }
}