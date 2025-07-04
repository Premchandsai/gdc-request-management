package com.gdc.requests_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.gdc.requests_management.repository")
public class RequestsManagementApplication {
	public static void main(String[] args) {
		SpringApplication.run(RequestsManagementApplication.class, args);
	}
}