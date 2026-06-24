package com.dist.key_value_service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(KeyNotFoundException.class)
    public ResponseEntity<String>handleKeyNotFoundException(KeyNotFoundException exe)
    {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exe.getMessage());
    }
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String>handleDuplicateKey(DuplicateKeyException ex){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());

    }

}
