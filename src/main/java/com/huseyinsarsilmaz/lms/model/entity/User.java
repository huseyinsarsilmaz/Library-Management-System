package com.huseyinsarsilmaz.lms.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "users")
public class User extends LmsEntity {

    private String email;
    private String password;
    private String roles;
    private String name;
    private String surname;
    private String phoneNumber;

    public enum UserRole {
        PATRON,
        LIBRARIAN,
    }

}
