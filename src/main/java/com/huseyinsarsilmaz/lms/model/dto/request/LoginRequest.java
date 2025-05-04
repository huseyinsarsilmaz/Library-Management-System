package com.huseyinsarsilmaz.lms.model.dto.request;

import com.huseyinsarsilmaz.lms.validation.RequiredField;
import com.huseyinsarsilmaz.lms.validation.StrSize;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @RequiredField(entityName = "User", fieldName = "email")
    @Email(message = "{fail.email.format}")
    private String email;

    @RequiredField(entityName = "User", fieldName = "password")
    @StrSize(entityName = "User", fieldName = "password", min = 8, max = 32)
    private String password;
}