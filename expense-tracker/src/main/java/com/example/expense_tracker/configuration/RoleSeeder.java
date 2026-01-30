package com.example.expense_tracker.configuration;

import com.example.expense_tracker.Repositories.RoleRepository;
import com.example.expense_tracker.entities.Role;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "docker", "local" })
@RequiredArgsConstructor
public class RoleSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        seed("ROLE_USER");
        seed("ROLE_ADMIN");
    }

    private void seed(String name) {
        roleRepository.findByName(name).orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            return roleRepository.save(r);
        });
    }
}
