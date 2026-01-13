package com.example.expense_tracker.mapper;

import com.example.expense_tracker.dto.authorization.RegisterDto;
import com.example.expense_tracker.entities.User;

public class UserMapper {
    public User toEntity(RegisterDto registerDto) {
        User user = new User();
        user.setEmail(registerDto.getEmail());
        user.setFullName(registerDto.getFullName());

        return user;
    }
}
