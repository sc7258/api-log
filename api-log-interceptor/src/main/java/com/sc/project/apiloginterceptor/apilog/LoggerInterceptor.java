package com.sc.project.apiloginterceptor.apilog;

import ch.qos.logback.core.util.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.DelegatingServerHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class LoggerInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;
    private final ApiLogRepository apiLogRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;
        val cachingRequest = (ContentCachingRequestWrapper) request;

        ApiLog apiLog = new ApiLog()
                .setServerIp(InetAddress.getLocalHost().getHostAddress())
                .setRequestUrl(cachingRequest.getRequestURL().toString())
                .setRequestMethod(cachingRequest.getMethod())
                .setClientIp(getClientIp(cachingRequest))
                //.setRequest(getRequestString(cachingRequest))
                .setRequest("")
                .setRequestTime(java.time.LocalDateTime.now())
                .setResponseTime(java.time.LocalDateTime.now())
                ;

        apiLogRepository.save(apiLog);
        request.setAttribute("requestId", apiLog.getRequestId());

        //return HandlerInterceptor.super.preHandle(request, response, handler);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        if (!(handler instanceof HandlerMethod)) return;

//        val cachingResponse = new ContentCachingResponseWrapper(response);
//        cachingResponse.copyBodyToResponse();
        val cachingRequest = (ContentCachingRequestWrapper) request;
        val cachingResponse = (ContentCachingResponseWrapper) response;
        val requestId = request.getAttribute("requestId");

        apiLogRepository.updateResponse(requestId.toString(), cachingResponse.getStatus(), new String(cachingResponse.getContentAsByteArray(), StandardCharsets.UTF_8));

        //HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);


    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //HandlerInterceptor.super.afterCompletion(request, response, handler, ex);

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
            //clientIp = request.getRemoteAddr();
            clientIp = InetAddress.getLocalHost().getHostAddress();
        }

//        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "127.0.0.1".equals(clientIp)) {
//            clientIp = InetAddress.getLocalHost().getHostAddress();
//        }

        return clientIp;
    }


    /**
     * convert request params into string
     * check!! - request.parameterMap.map { (key, value) -> "$key=${value.contentToString()}" }.joinToString(", ")
     * @param request
     * @return
     */
    private String getRequestParams(HttpServletRequest request) throws JsonProcessingException {
        //        val requestParams = request.parameterMap.map { (key, value) -> "$key=${value.contentToString()}" }.joinToString(", ")

//        return request.getParameterMap().entrySet().stream()
//                .map( i -> i.getKey() + "=" + Arrays.toString(i.getValue()))
//                .collect(Collectors.joining(", "));

        return objectMapper.writeValueAsString(request.getParameterMap());
    }

    /**
     * convert request body into string
     * check!! - objectMapper.readTree(requestWrapper.contents.toString(Charsets.UTF_8))
     * @param request
     * @return
     */
    private String getRequestBody(HttpServletRequest request) throws IOException {
//        val cachingRequest = (ContentCachingRequestWrapper) request;
//        return new String(cachingRequest.getContentAsByteArray(), StandardCharsets.UTF_8);

        val cachingRequest = new ContentCachingRequestWrapper(request);
        return  StreamUtils.copyToString(cachingRequest.getInputStream(), StandardCharsets.UTF_8);
    }

    private String getRequestString(HttpServletRequest request) throws IOException {
//        val requestParams = request.parameterMap.map { (key, value) -> "$key=${value.contentToString()}" }.joinToString(", ")
//        val requestBody = objectMapper.readTree(requestWrapper.contents.toString(Charsets.UTF_8))
        val requestParams = getRequestParams(request);
        val requestBody = getRequestBody(request);


        if (StringUtil.isNullOrEmpty(requestParams)) {
            return requestBody;
        }
        else if(StringUtil.isNullOrEmpty(requestBody)) {
            return requestParams;
        }
        else{
            return requestParams + ", " + requestBody;
        }
    }

//    private  String getResponseString(HttpServletResponse response) {
//        try {
//            ServletOutputStream ouputStream = response.getOutputStream();
//            return StreamUtils.copyToString(ouputStream, StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            return null;
//        }
//
//    }

//    private String getRequestString(ProceedingJoinPoint joinPoint){
//        final String EMPTY_STRING = "";
//
//        var signature = joinPoint.getSignature();
//        List<String> parameterNames = (signature instanceof MethodSignature)
//                ? new ArrayList<String>(List.of(((MethodSignature) signature).getParameterNames()))
//                : new ArrayList<String>();
//
//        //var parameterValues = new ArrayList<Object>(List.of(joinPoint.getArgs()));
//        var parameterValues = Stream.of(joinPoint.getArgs()).map(t -> {
//            try {
//                return mapper.writeValueAsString(t);
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }).toList();
//
//
//
//        return IntStream
//                .range(0, Math.min(parameterNames.size(), parameterValues.size()))
//                .mapToObj(i -> parameterNames.get(i) + "=" + parameterValues.get(i))
//                .collect(Collectors.joining(", ", EMPTY_STRING, EMPTY_STRING));
//    }
}
