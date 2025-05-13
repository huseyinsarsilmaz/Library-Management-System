package com.huseyinsarsilmaz.lms.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LmsApiResponse<T> {

    private boolean status;
    private String message;
    private T data;

}
