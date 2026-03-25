package com.example.demo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "{validation.user.username.required}")
    private String username;

    @NotBlank(message = "{validation.user.password.required}")
    private String password;

    private Boolean rememberMe = false;
}
