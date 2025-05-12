package com.huseyinsarsilmaz.lms.model.dto.request;

import com.huseyinsarsilmaz.lms.validation.Alphabethical;
import com.huseyinsarsilmaz.lms.validation.PhoneNumber;
import com.huseyinsarsilmaz.lms.validation.RequiredField;
import com.huseyinsarsilmaz.lms.validation.StrSize;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @RequiredField(entityName = "User", fieldName = "email")
    @Email(message = "{fail.email.format}")
    private String email;

    @RequiredField(entityName = "User", fieldName = "name")
    @StrSize(entityName = "User", fieldName = "name", min = 2, max = 32)
    @Alphabethical(entityName = "User", fieldName = "name")
    private String name;

    @RequiredField(entityName = "User", fieldName = "surname")
    @StrSize(entityName = "User", fieldName = "surname", min = 2, max = 32)
    @Alphabethical(entityName = "User", fieldName = "surname")
    private String surname;

    @RequiredField(entityName = "User", fieldName = "phone number")
    @PhoneNumber(entityName = "User", fieldName = "phone number")
    private String phoneNumber;
}
