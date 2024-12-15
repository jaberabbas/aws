package com.example.secret_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecretManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecretManagementApplication.class, args);
		System.out.println("Hello Amazon Secret Manager");
	}

}
