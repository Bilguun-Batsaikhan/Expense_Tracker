package com.example.expense_tracker.Services;

import java.time.LocalDateTime;
import java.util.UUID;


import com.example.expense_tracker.entities.User;

public class UserService {
    public User getCurrentUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        //setroles
        return user;
    }
}
