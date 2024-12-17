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
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
//@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Component
@WebFilter(filterName = "ContentCachingFilter ", urlPatterns = "/*")
public class ContentCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        log.debug("IN  ContentCachingFilter ");

        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(httpServletRequest);
        //cachingRequest.getInputStream(); //?? is necessary
        //log.debug(new String(cachingRequest.getContentAsByteArray()));

        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper (httpServletResponse);

        String url  = httpServletRequest.getRequestURI();
        String query  = httpServletRequest.getQueryString();
        String reqContent = new String(cachingRequest.getContentAsByteArray());
        //String reqContent = StreamUtils.copyToString(cachingRequest.getInputStream(), StandardCharsets.UTF_8);
        log.info("request url : {}, request query : {}, requsetBody : {}", url, query, reqContent);

        filterChain.doFilter(cachingRequest, cachingResponse);



        cachingResponse.copyBodyToResponse();
    }
}