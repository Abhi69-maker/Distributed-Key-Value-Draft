package com.dist.key_value_service.exception;

public class DuplicateKeyException extends RuntimeException{

    public DuplicateKeyException(String Key){
        super("Key Found : "+ Key);
    }
}
