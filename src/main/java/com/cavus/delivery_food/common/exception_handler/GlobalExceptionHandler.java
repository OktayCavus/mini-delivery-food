package com.cavus.delivery_food.common.exception_handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cavus.delivery_food.category.service.CategoryNotFoundException;
import com.cavus.delivery_food.common.response.BaseResponse;
import com.cavus.delivery_food.outlet.service.OutletExistException;
import com.cavus.delivery_food.outlet.service.OutletNotFoundException;
import com.cavus.delivery_food.product.service.ProductNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
    
}
