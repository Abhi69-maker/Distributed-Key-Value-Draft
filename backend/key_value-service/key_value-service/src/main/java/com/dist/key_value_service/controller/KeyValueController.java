package com.dist.key_value_service.controller;

import com.dist.key_value_service.dto.KeyValueDTO;
import com.dist.key_value_service.service.KeyValueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController("/api")
public class KeyValueController {

    KeyValueService keyValueService;


    @GetMapping("/getusers")
    public List<KeyValueDTO> getUsers(){

        return keyValueService.getAllUsers();
    }
    @GetMapping("/user/{key}")
    public KeyValueDTO getUserByKey(@PathVariable String key){
        return keyValueService.getUserByKey(key);
    }

    @PostMapping("/create")
    public KeyValueDTO create(@RequestBody KeyValueDTO keyValueDTO){
        return keyValueService.createuser(keyValueDTO);
    }
    @DeleteMapping("/delete")
    public KeyValueDTO delete(@RequestBody KeyValueDTO keyValueDTO){
        return keyValueService.deleteByUser(keyValueDTO);
    }


}
