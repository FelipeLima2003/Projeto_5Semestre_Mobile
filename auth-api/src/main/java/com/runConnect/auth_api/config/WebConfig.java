package com.runConnect.auth_api.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.upload-dir}")
    private String uploadDir;

    // --- MÉTODO PARA O CORS ---
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todas as rotas
                .allowedOrigins("*") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT");

    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        String uploadUri = uploadPath.toUri().toString();

        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadUri);
    }
}
