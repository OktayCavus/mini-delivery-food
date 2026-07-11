package com.cavus.delivery_food.auth.exceptions;

public class EmailAlreadyExistException extends RuntimeException{
    public EmailAlreadyExistException(String email) {
        super("Bu email zaten kullanılıyor " + email);
    }
}
