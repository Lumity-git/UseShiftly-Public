package com.hotel.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve frontend files from the frontend directory
        registry.addResourceHandler("/frontend/**")
                .addResourceLocations("file:frontend/", "classpath:/static/frontend/");
        
        // Serve static resources from root
        registry.addResourceHandler("/**")
                .addResourceLocations("file:./", "classpath:/static/");
    }
}
