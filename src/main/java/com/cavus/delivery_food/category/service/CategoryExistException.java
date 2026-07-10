package com.cavus.delivery_food.category.service;

/**
 * CategoryExistException
 */
public class CategoryExistException extends RuntimeException {

    public CategoryExistException(String name) {
        super("Kategori zaten mevcut: " + name);
    }

}
