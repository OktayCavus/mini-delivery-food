package com.cavus.delivery_food.category.exceptions;

import java.util.UUID;

public class CategoryNotFoundException  extends  RuntimeException{

    public CategoryNotFoundException(UUID uuid){

        super("kategori bulunamadı, id:" + uuid);
    }


}
