package com.example.project.service;

import com.example.project.DTO.RegistrationRequest;
import com.example.project.entity.Role;
import com.example.project.entity.User;
import com.example.project.entity.UserRole;
import com.example.project.repository.RoleRepository;
import com.example.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public String registerUser(RegistrationRequest request) {
        // Foydalanuvchi mavjudligini tekshirish
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Yangi foydalanuvchi yaratish
        User user = new User();
        List<UserRole> userRoles = new ArrayList<>();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Rollarni olish va tayinlash
        List<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .toList();

        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoles.add(userRole);
        }
        user.setUserRoles(userRoles);
        // Foydalanuvchini saqlash
        userRepository.save(user);

        return "User registered successfully";
    }
}
