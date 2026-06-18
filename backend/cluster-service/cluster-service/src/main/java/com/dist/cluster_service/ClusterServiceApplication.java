package com.dist.cluster_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClusterServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClusterServiceApplication.class, args);
		System.out.println("Cluster Service Application Started");

	}

}
