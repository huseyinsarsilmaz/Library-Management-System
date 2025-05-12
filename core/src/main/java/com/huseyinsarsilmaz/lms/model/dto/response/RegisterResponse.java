package com.huseyinsarsilmaz.lms.model.dto.response;

import com.huseyinsarsilmaz.lms.model.entity.User;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterResponse {
    private String email;
    private String name;
    private String surname;

    public RegisterResponse(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.surname = user.getSurname();
    }
}
