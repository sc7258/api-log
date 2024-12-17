package com.sc.project.apilogaop.apilog;

import ch.qos.logback.core.util.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * https://colabear754.tistory.com/204
 */

@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

    private final ObjectMapper mapper;
    private final ApiLogRepository apiLogRepository;


    @Around("execution(public * com.sc.project.apilogaop.controller.*.*(..))")
    public ResponseEntity<?> log(ProceedingJoinPoint joinPoint) throws UnknownHostException {
        //ApiLog apiLog = apiLogRepository.save(getApiLog(joinPoint));
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        var httpRequest = requestAttributes.getRequest();
        ApiLog apiLog = new ApiLog();
        apiLog.setServerIp(InetAddress.getLocalHost().getHostAddress());
        apiLog.setRequestUrl(httpRequest.getRequestURL().toString());
        apiLog.setRequestMethod(httpRequest.getMethod());
        apiLog.setClientIp(getClientIp(httpRequest));
        apiLog.setRequest(getRequestString(joinPoint));
        apiLog.setRequestTime(java.time.LocalDateTime.now());
        apiLog.setResponseTime(java.time.LocalDateTime.now());

        apiLogRepository.save(apiLog);

        try {
            var response = (ResponseEntity<?>)joinPoint.proceed();
            apiLogRepository.updateResponse(apiLog.getSeq(), response.getStatusCode().value(), mapper.writeValueAsString(Objects.requireNonNull(response.getBody())));
            return response;
        } catch (Throwable e) {
            apiLogRepository.updateResponse(apiLog.getSeq(), 500, StringUtil.isNullOrEmpty(e.getMessage()) ? "error" : e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private ApiLog getApiLog(ProceedingJoinPoint joinPoint) throws UnknownHostException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        var httpRequest = requestAttributes.getRequest();
        return new ApiLog()
                .setServerIp( InetAddress.getLocalHost().getHostAddress())
                .setRequestUrl(httpRequest.getRequestURL().toString())
                .setRequestMethod(httpRequest.getMethod())
                .setClientIp(getClientIp(httpRequest))
                .setRequest(getRequestString(joinPoint))
                ;
    }

//    private String getClientIp(HttpServletRequest httpRequest) {
//        String clientIp = httpRequest.getHeader("X-FORWARDED-FOR");
//        if (StringUtils.hasText(clientIp) && !"unknown".equalsIgnoreCase(clientIp)){
//            clientIp = httpRequest.getRemoteAddr();
//        }
//        return clientIp;
//    }

    private String getClientIp(HttpServletRequest request) throws UnknownHostException {
        String clientIp = null;
        boolean isIpInHeader = false;

        List<String> headerList = new ArrayList<>();
        headerList.add("X-Forwarded-For");
        headerList.add("HTTP_CLIENT_IP");
        headerList.add("HTTP_X_FORWARDED_FOR");
        headerList.add("HTTP_X_FORWARDED");
        headerList.add("HTTP_FORWARDED_FOR");
        headerList.add("HTTP_FORWARDED");
        headerList.add("Proxy-Client-IP");
        headerList.add("WL-Proxy-Client-IP");
        headerList.add("HTTP_VIA");
        headerList.add("IPV6_ADR");

        for (String header : headerList) {
            clientIp = request.getHeader(header);
            if (StringUtils.hasText(clientIp) && !"unknown".equalsIgnoreCase(clientIp)) {
                isIpInHeader = true;
                break;
            }
        }

        if (!isIpInHeader) {
            //clientIp = request.getRemoteAddr();
            clientIp = InetAddress.getLocalHost().getHostAddress();
        }

//        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "127.0.0.1".equals(clientIp)) {
//            clientIp = InetAddress.getLocalHost().getHostAddress();
//        }

        return clientIp;
    }

    private String getRequestString(ProceedingJoinPoint joinPoint){
        final String EMPTY_STRING = "";

        var signature = joinPoint.getSignature();
        List<String> parameterNames = (signature instanceof MethodSignature)
                ? new ArrayList<String>(List.of(((MethodSignature) signature).getParameterNames()))
                : new ArrayList<String>();

        //var parameterValues = new ArrayList<Object>(List.of(joinPoint.getArgs()));
        var parameterValues = Stream.of(joinPoint.getArgs()).map(t -> {
            try {
                return mapper.writeValueAsString(t);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).toList();



        return IntStream
                .range(0, Math.min(parameterNames.size(), parameterValues.size()))
                .mapToObj(i -> parameterNames.get(i) + "=" + parameterValues.get(i))
                .collect(Collectors.joining(", ", EMPTY_STRING, EMPTY_STRING));
    }

}
