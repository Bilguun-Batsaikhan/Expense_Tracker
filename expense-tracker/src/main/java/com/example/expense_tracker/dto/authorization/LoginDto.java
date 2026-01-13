package com.example.expense_tracker.dto.authorization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {
    @Email(message = "Email is in wrong format")
    private String email;
    @NotNull(message = "Password is required")
    private String password;
}
