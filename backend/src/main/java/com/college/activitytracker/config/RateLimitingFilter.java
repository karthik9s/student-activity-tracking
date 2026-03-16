package com.college.activitytracker.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long TIME_WINDOW_MS = 60000; // 1 minute

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Skip rate limiting for authentication endpoints
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.startsWith("/api/v1/auth/")) {
            logger.debug("Skipping rate limiting for authentication endpoint: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        String clientIp = getClientIP(httpRequest);
        
        if (isRateLimitExceeded(clientIp)) {
            logger.warn("Rate limit exceeded for IP {} on endpoint {}", clientIp, requestURI);
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("Rate limit exceeded. Please try again later.");
            return;
        }
        
        logger.debug("Rate limiting filter passed for IP {} on endpoint {}", clientIp, requestURI);
        chain.doFilter(request, response);
    }

    private boolean isRateLimitExceeded(String clientIp) {
        long currentTime = System.currentTimeMillis();
        
        RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());
        
        synchronized (counter) {
            // Reset counter if time window has passed
            if (currentTime - counter.windowStart > TIME_WINDOW_MS) {
                counter.count.set(0);
                counter.windowStart = currentTime;
            }
            
            int currentCount = counter.count.incrementAndGet();
            return currentCount > MAX_REQUESTS_PER_MINUTE;
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private static class RequestCounter {
        AtomicInteger count = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
    }
}
