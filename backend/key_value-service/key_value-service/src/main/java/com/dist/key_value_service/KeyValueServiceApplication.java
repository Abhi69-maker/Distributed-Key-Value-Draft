package com.dist.key_value_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

import java.util.TimeZone;

@SpringBootApplication
public class KeyValueServiceApplication {

	public static void main(String[] args) {

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		System.out.println("Timezone = " +
				TimeZone.getDefault().getID());

		SpringApplication.run(
				KeyValueServiceApplication.class,
				args
		);
	}
}