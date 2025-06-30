package com.banquito.formalizacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI formalizacionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Formalización - Préstamos Automotrices")
                        .description("Microservicio para la gestión del proceso de formalización de contratos " +
                                   "de crédito automotriz, incluyendo instrumentación, firma, desembolso y " +
                                   "administración de pagarés.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Banco BanQuito")
                                .email("soporte@banquito.com"))
                        .license(new License()
                                .name("Licencia Banco BanQuito")
                                .url("https://www.banquito.com/licencia")));
    }
} 