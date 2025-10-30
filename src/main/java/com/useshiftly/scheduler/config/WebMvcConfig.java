package com.useshiftly.scheduler.config;

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

        registry.addResourceHandler("/frontend/{name:[a-zA-Z0-9_-]+}")
                .addResourceLocations("file:frontend/", "classpath:/static/frontend/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver() {
                    @Override
                    protected org.springframework.core.io.Resource getResource(String resourcePath,
                            org.springframework.core.io.Resource location) throws java.io.IOException {
                        // Try with .html extension
                        org.springframework.core.io.Resource resource = location.createRelative(resourcePath + ".html");
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        // Fallback to default
                        return super.getResource(resourcePath, location);
                    }
                });
        
        // Serve static resources from root
        registry.addResourceHandler("/**")
                .addResourceLocations("file:./", "classpath:/static/");
    }
}
