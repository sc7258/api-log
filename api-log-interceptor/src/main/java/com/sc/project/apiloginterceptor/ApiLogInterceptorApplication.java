package com.sc.project.apiloginterceptor;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
public class ApiLogInterceptorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiLogInterceptorApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // timezone 설정
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        //TimeZone.setDefault(TimeZone.getTimeZone("GMT+09:00")); //KST
    }

}
