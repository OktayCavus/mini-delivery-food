package com.cavus.delivery_food.auth.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Geçersiz refresh token");
    }
}
