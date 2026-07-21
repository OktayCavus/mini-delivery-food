package com.cavus.delivery_food.auth.exceptions;

public class ExpiredRefreshTokenException extends RuntimeException {

    public ExpiredRefreshTokenException() {
        super("Refresh token süresi dolmuş");
    }
}
