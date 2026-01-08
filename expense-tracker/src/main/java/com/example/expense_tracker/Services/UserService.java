package com.example.expense_tracker.Services;


import org.springframework.stereotype.Service;

import com.example.expense_tracker.Repositories.UserRepository;
import com.example.expense_tracker.entities.User;
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
public User getCurrentUser() {
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users in DB"));
    }
}
