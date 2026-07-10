package com.cavus.delivery_food.category.service;

import java.util.UUID;

public class CategoryNotFoundException  extends  RuntimeException{

    public CategoryNotFoundException(UUID uuid){

        super("kategori bulunamadı, id:" + uuid);
    }


}
