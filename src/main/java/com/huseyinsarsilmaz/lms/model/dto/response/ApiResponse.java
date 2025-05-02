package com.huseyinsarsilmaz.lms.model.dto.response;

import lombok.Data;

@Data
public class ApiResponse {

    private final boolean status;
    private final String message;
    private final Object data;
}
