package com.dist.key_value_service.exception;

public class KeyNotFoundException extends  RuntimeException{

    public KeyNotFoundException(String key){
        super("Key Not Found: "+ key);
    }
}
