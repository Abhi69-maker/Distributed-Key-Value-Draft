package com.dist.key_value_service.controller;


import com.dist.key_value_service.entity.KeyValue;
import com.dist.key_value_service.service.KeyValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.List;


@RestController

@RequiredArgsConstructor
@RequestMapping("/api")
public class KeyValueController {

    private final KeyValueService keyValueService;


    @GetMapping("/getusers")
    public ResponseEntity<List<KeyValue>> getUsers(){


        return ResponseEntity.ok(keyValueService.getAllUsers());
    }


    @GetMapping("/user/{key}")
    public ResponseEntity<KeyValue> getUserByKey(@PathVariable String key){

        return ResponseEntity.ok(
                keyValueService.getUserByKey(key));
    }

    @PostMapping("/create")
    public ResponseEntity<KeyValue> createUser(@RequestBody KeyValue keyValue){

        return ResponseEntity.ok(
                keyValueService.createUser(keyValue));


    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<KeyValue> delete(@PathVariable String id){

        return ResponseEntity.ok(
                keyValueService.deleteByKey(id)
        );
    }


}
