package com.huseyinsarsilmaz.lms.model.dto.request;

import com.huseyinsarsilmaz.lms.validation.RequiredField;
import com.huseyinsarsilmaz.lms.validation.StrSize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateRequest {

    @RequiredField(entityName = "User", fieldName = "old password")
    @StrSize(entityName = "User", fieldName = "old password", min = 8, max = 32)
    private String oldPassword;

    @RequiredField(entityName = "User", fieldName = "new password")
    @StrSize(entityName = "User", fieldName = "new password", min = 8, max = 32)
    private String newPassword;

}
