package com.example.expense_tracker.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expense_tracker.Services.JwtService;
import com.example.expense_tracker.Services.UserService;
import com.example.expense_tracker.dto.authorization.LoginDto;
import com.example.expense_tracker.dto.authorization.LoginResponseDto;
import com.example.expense_tracker.dto.authorization.RegisterDto;
import com.example.expense_tracker.entities.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthenticationController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterDto registerDto) {
        return ResponseEntity.ok(userService.registerUser(registerDto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@Valid @RequestBody LoginDto loginDto) {
        User user = userService.loginUser(loginDto);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }
}
