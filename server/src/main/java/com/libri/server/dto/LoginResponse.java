package com.libri.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String role;
    private String firstName;
    private String lastName;
    private String email;
}
