package com.sc.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ApiLogAopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiLogAopApplication.class, args);
    }

}
