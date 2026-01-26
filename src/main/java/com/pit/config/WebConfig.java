package com.pit.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


// Application component.
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    // Handles add cors mappings request operation
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("GET","POST","PUT","DELETE","PATCH");
    }
}
