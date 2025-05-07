package com.huseyinsarsilmaz.lms.model.dto.request;

import com.huseyinsarsilmaz.lms.validation.RequiredField;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BorrowRequest {

    private Long borrowerId;

    @RequiredField(entityName = "Book", fieldName = "id")
    private Long bookId;

}
