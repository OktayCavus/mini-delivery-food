package com.cavus.delivery_food.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deliveryFoodOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Delivery Food API")
                        .description("Delivery Food uygulaması için REST API dokümantasyonu")
                        .version("v1"));
    }
}
