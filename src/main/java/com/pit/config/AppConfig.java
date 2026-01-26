package com.pit.config;

import com.pit.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ JwtProperties.class })
public class AppConfig { }
