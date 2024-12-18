package com.sc.project.apiloginterceptor.apilog;

import ch.qos.logback.core.util.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class LoggerInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;
    private final ApiLogRepository apiLogRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("traceId", UUID.randomUUID().toString());
        log.debug("preHandle, traceId:{}", request.getAttribute("traceId"));

        if (!(handler instanceof HandlerMethod)) return true;

        final Jwt jwtToken = ((Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal());

        //log.info("id = {}", user.getId());
        val userId = jwtToken.getClaims().getOrDefault("sub", "");
        val userName = jwtToken.getClaims().getOrDefault("preferred_username", "");
        log.info("id={}", userId);
        log.info("preferred_username = {}", userName);

        val cachingRequest = (ContentCachingRequestWrapper) request;
        //val cachingResponse = (ContentCachingResponseWrapper) response;

        ApiLog apiLog = new ApiLog()
                .setTraceId(request.getAttribute("traceId").toString())
                .setServerIp(InetAddress.getLocalHost().getHostAddress())
                .setRequestUrl(getRequestUrl(cachingRequest))
                .setRequestMethod(cachingRequest.getMethod())
                .setClientIp(getClientIp(cachingRequest))
                .setRequest(getRequestBody(cachingRequest))
                //.setResponseStatus(cachingResponse.getStatus())
                //.setResponse(getResponseBody(cachingResponse))
                .setRequestTime(java.time.LocalDateTime.now())
                //.setResponseTime(java.time.LocalDateTime.now())
                .setUserId(userId.toString())
                .setUserName(userName.toString())
                ;

        apiLogRepository.save(apiLog);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.debug("postHandle, traceId:{}", request.getAttribute("traceId"));
        if (!(handler instanceof HandlerMethod)) return;

//        if (!(request instanceof ContentCachingRequestWrapper)) return;
//        if (!(response instanceof ContentCachingResponseWrapper)) return;

        val cachingRequest = (ContentCachingRequestWrapper) request;
        val cachingResponse = (ContentCachingResponseWrapper) response;

        apiLogRepository.updateResponse(
                cachingRequest.getAttribute("traceId").toString(),
                cachingResponse.getStatus(),
                getResponseBody(cachingResponse)
        );
    }

    /**
     * 성공/실패에 상관없이 이 핸들러로 처리가 가능하다!
     * check!! - 왜 오류가 났을때도 response status가 200으로 설정되어 있는거지??
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * execution, for type and/or instance examination
     * @param ex any exception thrown on handler execution, if any; this does not
     * include exceptions that have been handled through an exception resolver
     * @throws Exception
     */

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.debug("afterCompletion, traceId:{}", request.getAttribute("traceId"));

        if (!(handler instanceof HandlerMethod)) return;

//        if (!(request instanceof ContentCachingRequestWrapper)) return;
//        if (!(response instanceof ContentCachingResponseWrapper)) return;

        val cachingRequest = (ContentCachingRequestWrapper) request;
        val cachingResponse = (ContentCachingResponseWrapper) response;

        apiLogRepository.updateRequest(
                cachingRequest.getAttribute("traceId").toString(),
                getRequestBody(cachingRequest)
        );
    }


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
            clientIp = InetAddress.getLocalHost().getHostAddress();
        }

        return clientIp;
    }

    /**
     * convert request body into string
     * check!! - objectMapper.readTree(requestWrapper.contents.toString(Charsets.UTF_8))
     * @param request
     * @return
     */
    private String getRequestBody(HttpServletRequest request) throws IOException {
        val cachingRequest = (ContentCachingRequestWrapper) request;
        return new String(cachingRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * get request url with query string
     * @param request
     * @return
     */
    private String getRequestUrl(HttpServletRequest request) {
        val cachingRequest = (ContentCachingRequestWrapper) request;

        return StringUtil.isNullOrEmpty(cachingRequest.getQueryString())
                ? cachingRequest.getRequestURL().toString()
                : cachingRequest.getRequestURL().toString() + "?" + cachingRequest.getQueryString();
    }

    private String getResponseBody(HttpServletResponse response) {
        val cachingResponse = (ContentCachingResponseWrapper) response;

        return new String(cachingResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
    }

}
