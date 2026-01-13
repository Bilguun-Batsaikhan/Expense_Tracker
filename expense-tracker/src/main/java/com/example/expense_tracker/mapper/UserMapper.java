package com.example.expense_tracker.mapper;

import org.springframework.stereotype.Component;

import com.example.expense_tracker.dto.authorization.RegisterDto;
import com.example.expense_tracker.entities.User;

@Component
public class UserMapper {
    public User toEntity(RegisterDto registerDto) {
        User user = new User();
        user.setEmail(registerDto.getEmail());
        user.setFullName(registerDto.getFullName());

        return user;
    }
}
