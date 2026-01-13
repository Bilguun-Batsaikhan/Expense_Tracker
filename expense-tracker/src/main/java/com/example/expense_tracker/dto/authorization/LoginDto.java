package com.example.expense_tracker.dto.authorization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    @Email(message = "Email is in wrong format")
    private String email;
    @NotNull(message = "Password is required")
    private String password;
}
