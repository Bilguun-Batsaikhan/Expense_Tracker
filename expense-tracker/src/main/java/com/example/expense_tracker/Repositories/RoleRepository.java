package com.example.expense_tracker.Repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_tracker.entities.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    public Optional<Role> findByName(String name);
}
