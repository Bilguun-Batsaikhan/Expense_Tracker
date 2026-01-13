package com.example.expense_tracker.Services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.expense_tracker.Repositories.UserRepository;
import com.example.expense_tracker.dto.authorization.LoginDto;
import com.example.expense_tracker.dto.authorization.RegisterDto;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;
import com.example.expense_tracker.mapper.UserMapper;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User getCurrentUser() {
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users in DB"));
    }

    public String registerUser(RegisterDto registerDto) {
        User user = userMapper.toEntity(registerDto);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        userRepository.save(user);

        return "Registered Succesfully!";
    }

    public String loginUser(LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.AUTHENTICATION_FAILED));
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED);
        }
        return "Login successful";
    }
}
