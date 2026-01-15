package com.example.expense_tracker.filters;

import java.io.IOException;
// import java.util.List;
// import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.example.expense_tracker.Services.JwtService;
import com.example.expense_tracker.Services.UserService;
import com.example.expense_tracker.dto.CustomUserDetails;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;

// import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final HandlerExceptionResolver resolver;

    public TokenValidationFilter(JwtService jwtService,
            UserService userService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.resolver = resolver;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);

            // If token is invalid, throw custom ApiException
            if (!jwtService.isTokenValid(token)) {
                throw new ApiException(ErrorCode.AUTHORIZATION_FAILED);
            }

            // Claims claims = jwtService.extractAllClaims(token);
            String email = jwtService.extractEmail(token);
            // UUID userId = UUID.fromString(claims.get("userId", String.class));
            User user = userService.getUser(email);

            CustomUserDetails userDetails = new CustomUserDetails(user);
            // List<GrantedAuthority> authorities = jwtService.extractRoles(claims)
            // .stream()
            // .<GrantedAuthority>map(SimpleGrantedAuthority::new)
            // .toList();

            // UsernamePasswordAuthenticationToken authentication = new
            // UsernamePasswordAuthenticationToken(
            // userId,
            // null,
            // authorities);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // it hands the exception over to RestControllerAdvice
            resolver.resolveException(request, response, null, e);
        }
    }
}
