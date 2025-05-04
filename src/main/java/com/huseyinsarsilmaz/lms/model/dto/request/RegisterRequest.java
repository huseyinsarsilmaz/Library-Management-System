package com.huseyinsarsilmaz.lms.model.dto.request;

import com.huseyinsarsilmaz.lms.validation.RequiredField;
import com.huseyinsarsilmaz.lms.validation.StrSize;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequest {

    @Email(message = "{fail.email.format}")
    @RequiredField(entityName = "User", fieldName = "email")
    private String email;

    @RequiredField(entityName = "User", fieldName = "password")
    @StrSize(entityName = "User", fieldName = "password", min = 8, max = 32)
    private String password;

    @RequiredField(entityName = "User", fieldName = "name")
    private String name;

    @RequiredField(entityName = "User", fieldName = "surname")
    private String surname;

    @RequiredField(entityName = "User", fieldName = "phone number")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid and contain 10 to 15 digits")
    private String phoneNumber;

}
