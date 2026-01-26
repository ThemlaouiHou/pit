package com.pit.config;


import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;


@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("PIT API")
                .description("Points d'Intérêt Touristiques – API REST")
                .version("v1"));
    }
}