package com.example.expense_tracker.filters;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Order(1)
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public final static String CORRELATION_ID = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = Optional.ofNullable(request.getHeader(CORRELATION_ID))
                .orElse(UUID.randomUUID().toString());

        MDC.put(CORRELATION_ID, correlationId);
        response.addHeader(CORRELATION_ID, correlationId);
        try {
            doFilter(request, response, filterChain);
        } finally {
            MDC.clear();
        }
    }

}
