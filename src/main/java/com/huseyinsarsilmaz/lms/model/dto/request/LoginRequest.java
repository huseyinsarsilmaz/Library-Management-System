package com.huseyinsarsilmaz.lms.model.dto.request;

import com.huseyinsarsilmaz.lms.validation.RequiredField;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @RequiredField(entityName = "User", fieldName = "email")
    @Email(message = "Invalid email format")
    private String email;

    @RequiredField(entityName = "User", fieldName = "password")
    @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters")
    private String password;
}