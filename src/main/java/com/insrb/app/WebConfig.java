package com.insrb.app;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class WebConfig implements WebMvcConfigurer {

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:3000", "http://192.168.0.105:3000", "https://insurobo-next.vercel.app", "https://localhost:3000", "https://192.168.0.105:3000")
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .maxAge(3600);
//    }
}
