package com.cavus.delivery_food.outlet.service;

public class OutletExistException extends RuntimeException {

    public OutletExistException() {
        super("Outlet zaten mevcut");
    }
    
}
