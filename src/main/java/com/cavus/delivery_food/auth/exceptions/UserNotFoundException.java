package com.cavus.delivery_food.auth.exceptions;

public class UserNotFoundException extends RuntimeException{
    
    public UserNotFoundException(String email) {
        super("Bu email ile kayıtlı kullanıcı bulunamadı " + email);
    }
}
