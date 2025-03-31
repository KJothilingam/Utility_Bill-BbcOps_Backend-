package com.BBC_Ops.BBC_Ops.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebSecurityConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Allow all endpoints
                .allowedOrigins("http://localhost:4200", "http://localhost:5200") // Add 5200
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Include OPTIONS for preflight
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true); // Allow cookies if needed
    }
}
