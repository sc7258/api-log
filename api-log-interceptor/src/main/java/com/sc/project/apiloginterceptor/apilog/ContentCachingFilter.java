package com.sc.project.apiloginterceptor.apilog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
@Order(value = Ordered.LOWEST_PRECEDENCE)
@Component
@WebFilter(filterName = "ContentCachingFilter ", urlPatterns = "/*")
public class ContentCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        log.debug("IN  ContentCachingFilter ");

        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(httpServletRequest);

        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper (httpServletResponse);

//        String url  = httpServletRequest.getRequestURI();
//        String query  = httpServletRequest.getQueryString();
//        String reqContent = new String(cachingRequest.getContentAsByteArray());
//        log.info("request url : {}, request query : {}, requsetBody : {}", url, query, reqContent);

        filterChain.doFilter(cachingRequest, cachingResponse);



        cachingResponse.copyBodyToResponse();
    }
}