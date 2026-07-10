package com.cavus.delivery_food.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cavus.delivery_food.common.response.BaseResponse;

@RestControllerAdvice
public class GlobalAuthExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<BaseResponse<Void>> handleEmailAlreadyExistException(EmailAlreadyExistException ex) {
        return ResponseEntity.badRequest().body(BaseResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.badRequest().body(BaseResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidPasswordException(InvalidPasswordException ex) {
        return ResponseEntity.badRequest().body(BaseResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }
    
}
