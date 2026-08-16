package com.kimangaVictor.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS policy.
 *
 * <p>Replaces the old per-controller {@code @CrossOrigin("https://10krhinooo.github.io/")}. That
 * value had a trailing slash, which never matches the browser's {@code Origin} header (origins are
 * scheme + host + port only), so every cross-origin contact submission was being rejected.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final PortfolioProperties properties;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(properties.cors().allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Content-Type", "X-API-Key")
                .maxAge(3600);
    }
}
