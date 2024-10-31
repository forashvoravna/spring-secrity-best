package com.example.project.DTO;

import lombok.Data;

import java.util.List;


@Data
public class RegistrationRequest {
    private String username;
    private String password;
    private List<String> roles;
}
