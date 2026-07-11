package com.cavus.delivery_food.auth.exceptions;

public class InvalidPasswordException extends RuntimeException{

    public InvalidPasswordException(String email) {
        super("Bu email ile kayıtlı kullanıcının şifresi hatalı " + email);
    }
    
}
