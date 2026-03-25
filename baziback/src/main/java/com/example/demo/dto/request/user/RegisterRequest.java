package com.example.demo.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "{validation.user.username.required}")
    @Size(min = 3, max = 20, message = "{validation.user.username.size}")
    private String username;

    @NotBlank(message = "{validation.user.password.required}")
    @Size(min = 6, max = 20, message = "{validation.user.password.size}")
    private String password;

    @Email(message = "{validation.user.email.invalid}")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "{validation.user.phone.pattern}")
    private String phone;

    private String code;
}
