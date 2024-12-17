package com.sc.project.apiloginterceptor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ApiLogInterceptorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiLogInterceptorApplication.class, args);
    }

}
