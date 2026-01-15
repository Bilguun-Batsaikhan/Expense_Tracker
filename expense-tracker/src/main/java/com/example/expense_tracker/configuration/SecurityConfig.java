package com.example.expense_tracker.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;
import com.example.expense_tracker.filters.TokenValidationFilter;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
        private final HandlerExceptionResolver resolver;

        public SecurityConfig(HandlerExceptionResolver resolver) {
                this.resolver = resolver;
        }

        // makes password Encoder injectable everywhere
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenValidationFilter tokenValidationFilter)
                        throws Exception {

                http
                                // Disable CSRF (JWT = stateless)
                                .csrf(csrf -> csrf.disable())

                                // No HTTP Session
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Endpoint security rules
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/auth/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .anyRequest().authenticated())

                                // Register JWT filter
                                .addFilterBefore(
                                                tokenValidationFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        // This part runs ONLY if the user didn't provide a token
                                                        // and tried to access a protected URL.
                                                        resolver.resolveException(request, response, null,
                                                                        new ApiException(
                                                                                        ErrorCode.AUTHORIZATION_FAILED));
                                                }));

                return http.build();
        }

        @Bean
        public OpenAPI customizeOpenAPI() {
                final String securitySchemeName = "bearerAuth";
                return new OpenAPI()
                                .addSecurityItem(new SecurityRequirement()
                                                .addList(securitySchemeName))
                                .components(new Components()
                                                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                                                .name(securitySchemeName)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")));
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

}
