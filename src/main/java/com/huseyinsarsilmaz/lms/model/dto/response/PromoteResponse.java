package com.huseyinsarsilmaz.lms.model.dto.response;

import com.huseyinsarsilmaz.lms.model.entity.User;

import lombok.Data;

@Data
public class PromoteResponse {
    private final String email;
    private final String name;
    private final String surname;

    public PromoteResponse(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.surname = user.getSurname();
    }
}
