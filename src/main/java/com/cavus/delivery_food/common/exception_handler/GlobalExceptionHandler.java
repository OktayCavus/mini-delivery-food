package com.cavus.delivery_food.common.exception_handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.cavus.delivery_food.auth.exceptions.EmailAlreadyExistException;
import com.cavus.delivery_food.auth.exceptions.InvalidPasswordException;
import com.cavus.delivery_food.auth.exceptions.UserNotFoundException;
import com.cavus.delivery_food.category.exceptions.CategoryExistException;
import com.cavus.delivery_food.category.exceptions.CategoryNotFoundException;
import com.cavus.delivery_food.common.response.BaseResponse;
import com.cavus.delivery_food.outlet.exceptions.OutletExistException;
import com.cavus.delivery_food.outlet.exceptions.OutletNotFoundException;
import com.cavus.delivery_food.product.exceptions.ProductNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BaseResponse.error(403, "Bu işlem için yetkiniz yok"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnexpected(Exception ex) {
        return ResponseEntity.status(500).body(BaseResponse.error(500, "Beklenmeyen bir hata oluştu"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.badRequest().body(BaseResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidPasswordException(InvalidPasswordException ex) {
        return ResponseEntity.badRequest().body(BaseResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(400, ex.getMessage()));
    }

     @ExceptionHandler(ProductNotFoundException.class)
     public ResponseEntity<BaseResponse<Void>> handleNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error(404, ex.getMessage()));
     }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNotFound(CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(OutletNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNotFound(OutletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error(404, ex.getMessage()));
    }
    
    @ExceptionHandler(OutletExistException.class)
    public ResponseEntity<BaseResponse<Void>> handleExist(OutletExistException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponse.error(409, ex.getMessage()));
    }
    
    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<BaseResponse<Void>> handleExist(EmailAlreadyExistException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponse.error(409, ex.getMessage()));
    }

    @ExceptionHandler(CategoryExistException.class)
    public ResponseEntity<BaseResponse<Void>> handleExist(CategoryExistException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponse.error(409, ex.getMessage()));
    }
    
}
