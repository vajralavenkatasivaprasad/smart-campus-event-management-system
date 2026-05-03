package com.campus.ems.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RegisterRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @Size(min = 6) @NotBlank private String password;
    private String phone;
    private String role;
}
