package com.huseyinsarsilmaz.lmsr.model.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    private String email;
    private String password;
    private String roles;
    private String name;
    private String surname;
    private String phoneNumber;

    @Id
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Role {
        ROLE_PATRON,
        ROLE_LIBRARIAN,
        ROLE_ADMIN
    }

    @Builder.Default
    private Boolean isActive = true;

}
