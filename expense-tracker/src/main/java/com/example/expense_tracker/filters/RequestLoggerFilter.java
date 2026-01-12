package com.example.expense_tracker.filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Order(2)
@Component
public class RequestLoggerFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggerFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            if (status >= 500) {
                logger.error("{} {} -> {} ({} ms)", method, uri, status, duration);
            } else if (status >= 400) {
                logger.warn("{} {} -> {} ({} ms)", method, uri, status, duration);
            } else {
                logger.info("{} {} -> {} ({} ms)", method, uri, status, duration);
            }
        }
    }
}
