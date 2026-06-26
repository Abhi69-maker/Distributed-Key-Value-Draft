package com.dist.key_value_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

import java.util.TimeZone;

@SpringBootApplication
public class KeyValueServiceApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // print env vars to confirm they're loaded
        System.out.println("=== ENV CHECK ===");
        System.out.println("SERVER_PORT  = " + System.getenv("SERVER_PORT"));
        System.out.println("NODE_ID      = " + System.getenv("NODE_ID"));
        System.out.println("DB_URL       = " + System.getenv("DB_URL"));
        System.out.println("DB_PASSWORD  = " + System.getenv("DB_PASSWORD"));
        System.out.println("=================");

        SpringApplication.run(KeyValueServiceApplication.class, args);
    }
}