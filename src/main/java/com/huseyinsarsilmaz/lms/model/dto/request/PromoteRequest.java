package com.huseyinsarsilmaz.lms.model.dto.request;

import com.huseyinsarsilmaz.lms.model.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PromoteRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "New Role is required")
    private User.Role newRole;
}