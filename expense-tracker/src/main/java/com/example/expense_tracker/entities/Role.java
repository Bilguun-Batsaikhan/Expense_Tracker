package com.example.expense_tracker.entities;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table
@Entity
public class Role {
    @Id
    @UuidGenerator
    private UUID id;
    @NotNull
    private String name;
    // removed users
}
