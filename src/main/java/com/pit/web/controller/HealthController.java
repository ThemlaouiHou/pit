package com.pit.web.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


// Application component.
@RestController
public class HealthController {
    @GetMapping("/api/health")
    // Handles health request operation
    public Map<String,String> health(){ return Map.of("status","OK"); }
}
