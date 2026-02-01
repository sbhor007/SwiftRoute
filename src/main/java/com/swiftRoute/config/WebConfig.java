package com.swiftRoute.config;

import com.swiftRoute.interceptor.JwtBlacklistInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtBlacklistInterceptor jwtBlacklistInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtBlacklistInterceptor)
                .addPathPatterns("/api/**");
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
