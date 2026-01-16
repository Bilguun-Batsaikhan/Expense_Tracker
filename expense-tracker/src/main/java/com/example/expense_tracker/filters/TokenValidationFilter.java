package com.example.expense_tracker.filters;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.expense_tracker.Services.JwtService;
import com.example.expense_tracker.Services.UserService;
import com.example.expense_tracker.dto.CustomUserDetails;
import com.example.expense_tracker.entities.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public TokenValidationFilter(JwtService jwtService,
            UserService userService) {
        this.jwtService = jwtService;

        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            // Explicitly fail auth (so EntryPoint handles it as 401)
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid JWT");
        }

        String email = jwtService.extractEmail(token);
        User user = userService.getUser(email);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);

    }
}
