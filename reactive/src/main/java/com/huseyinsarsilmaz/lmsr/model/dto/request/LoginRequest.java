package com.huseyinsarsilmaz.lmsr.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters")
    private String password;
}