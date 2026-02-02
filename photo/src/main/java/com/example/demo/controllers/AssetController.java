package com.example.demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AssetController {
    public ResponseEntity<?> uploadAssets(MultipartFile[] files, String metadata);
    
}
    // @Bean
    // public WebMvcConfigurer corsConfigurer() {
    //     @Override
    //     public void addCorsMappings(CorsRegistry registry) {
    //         registry.addMapping("/**").allowedOrigins("http://localhost:3000")
    //     }
    // }

