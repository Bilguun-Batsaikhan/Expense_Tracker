package com.example.expense_tracker.Services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.expense_tracker.Repositories.RoleRepository;
import com.example.expense_tracker.Repositories.UserRepository;
import com.example.expense_tracker.dto.CustomUserDetails;
import com.example.expense_tracker.dto.authorization.LoginDto;
import com.example.expense_tracker.dto.authorization.RegisterDto;
import com.example.expense_tracker.entities.Role;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.enums.ErrorCode;
import com.example.expense_tracker.exceptions.ApiException;
import com.example.expense_tracker.mapper.UserMapper;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    // public User getCurrentUser() {
    // UUID id = (UUID) SecurityContextHolder.getContext()
    // .getAuthentication()
    // .getPrincipal();
    // User user = userRepository.findById(id).orElseThrow(() -> new
    // ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    // return user;
    // }

    public CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.AUTHORIZATION_FAILED);
        }

        return (CustomUserDetails) authentication.getPrincipal();
    }

    public String registerUser(RegisterDto registerDto) {
        User user = userMapper.toEntity(registerDto);
        user.setEnabled(true); // new user is always enabled
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        Role userRole = roleRepository
                .findByName("ROLE_USER")
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        user.getRoles().add(userRole);

        userRepository.save(user);

        return "Registered Succesfully!";
    }

    public User loginUser(LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.AUTHENTICATION_FAILED));
        if (!user.isEnabled()) {
            throw new ApiException(ErrorCode.USER_DISABLED);
        }

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED);
        }
        return user;
    }

    public User getUser(String Email) {
        return userRepository.findByEmail(Email).orElseThrow(() -> new ApiException(ErrorCode.AUTHENTICATION_FAILED));
    }
}
