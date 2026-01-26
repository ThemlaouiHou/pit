package com.pit.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Application component.
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long expirationMs;

    // Handles get secret request operation
    public String getSecret() { return secret; }
    // Handles set secret request operation
    public void setSecret(String secret) { this.secret = secret; }

    // Handles get expiration ms request operation
    public long getExpirationMs() { return expirationMs; }
    // Handles set expiration ms request operation
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
}
