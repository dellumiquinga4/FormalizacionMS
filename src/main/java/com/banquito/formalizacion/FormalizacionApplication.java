package com.banquito.formalizacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FormalizacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormalizacionApplication.class, args);
	}

}
