package com.cavus.delivery_food.outlet.exceptions;

public class OutletExistException extends RuntimeException {

    public OutletExistException() {
        super("Outlet zaten mevcut");
    }
    
}
