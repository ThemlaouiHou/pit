package com.pit;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// Main Spring Boot application entrypoint.
@SpringBootApplication
public class PitApplication {
    // Handles main request operation
    public static void main(String[] args) {
        SpringApplication.run(PitApplication.class, args);
    }
}
