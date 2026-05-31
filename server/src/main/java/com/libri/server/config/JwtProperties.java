package com.libri.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    private String secret = "LibriSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026";
    private long expiration = 86400000L;
}
