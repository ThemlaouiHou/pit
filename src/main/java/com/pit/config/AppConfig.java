package com.pit.config;

import com.pit.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Application component.
@Configuration
@EnableConfigurationProperties({ JwtProperties.class })
public class AppConfig { }
