package com.dist.metrics_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MetricsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetricsServiceApplication.class, args);
	}

}
