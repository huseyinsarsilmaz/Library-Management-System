package com.huseyinsarsilmaz.lms.model.dto.request;

import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.validation.RequiredField;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PromoteRequest {

    @RequiredField(entityName = "User", fieldName = "email")
    @Email(message = "Invalid email format")
    private String email;

    @RequiredField(entityName = "User", fieldName = "new role")
    private User.Role newRole;
}