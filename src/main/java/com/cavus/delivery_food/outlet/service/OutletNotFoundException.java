package com.cavus.delivery_food.outlet.service;

import java.util.UUID;

public class OutletNotFoundException extends RuntimeException{

    public OutletNotFoundException(UUID id) {
        super("Outlet bulunamadı, id: " + id);
    }
    
}
