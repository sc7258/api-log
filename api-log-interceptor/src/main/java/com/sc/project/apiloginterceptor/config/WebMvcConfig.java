package com.sc.project.apiloginterceptor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sc.project.apiloginterceptor.apilog.ApiLogRepository;
import com.sc.project.apiloginterceptor.apilog.LoggerInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final ApiLogRepository apiLogRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggerInterceptor(objectMapper, apiLogRepository))
                //.excludePathPatterns("/css/**", "/images/**", "/js/**")
                .addPathPatterns("/questions/**")
        ;

        //WebMvcConfigurer.super.addInterceptors(registry);
    }
}
