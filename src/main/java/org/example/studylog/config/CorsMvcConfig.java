package org.example.studylog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .exposedHeaders("Set-Cookie")
                .allowedOrigins("http://localhost:5174", "https://web.studylog.shop")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"); // PATCH & OPTIONS 포함;
    }
}
