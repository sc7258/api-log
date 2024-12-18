package com.sc.project.apiloginterceptor.apilog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;


/**
 * https://colabear754.tistory.com/204
 */

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "api_logs")
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(unique = true, nullable = false)
    private String traceId;

    private String serverIp;
    @Column(length = 4096, nullable = false)
    private String requestUrl;
    @Column(nullable = false)
    private String requestMethod;


    private Integer responseStatus;
    @Column(nullable = false)
    private String clientIp;
    @Column(length = 4096, nullable = false)
    private String request;
    @Column(length = 4096)
    private String response;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime requestTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    @LastModifiedDate
    private LocalDateTime responseTime;
}
