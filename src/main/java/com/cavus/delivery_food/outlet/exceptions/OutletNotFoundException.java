package com.cavus.delivery_food.outlet.exceptions;

import java.util.UUID;

public class OutletNotFoundException extends RuntimeException{

    public OutletNotFoundException(UUID id) {
        super("Outlet bulunamadı, id: " + id);
    }
    
}
