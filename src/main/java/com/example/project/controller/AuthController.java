package com.example.project.controller;

import com.example.project.DTO.*;
import com.example.project.entity.Role;
import com.example.project.entity.UserRole;
import com.example.project.repository.UserRepository;
import com.example.project.service.UserService;
import com.example.project.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    private final UserRepository userRepository;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String jwt = jwtTokenUtil.generateAccessToken(userDetails.getUsername(), roles);
        String rjwt = jwtTokenUtil.generateRefreshToken(userDetails.getUsername());

        return ResponseEntity.ok(new AuthResponse(jwt,rjwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        try {
            String result = userService.registerUser(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (jwtTokenUtil.validateJwtToken(refreshToken)) {
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            List<String> roles = Objects.requireNonNull(userRepository.findByUsername(username).orElse(null)).getUserRoles().stream()
                    .map(UserRole::getRole).map(Role::getName).collect(Collectors.toList());

                    // Yangi access token yaratish
                    String newAccessToken = jwtTokenUtil.generateAccessToken(username, roles);
            return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }
}
