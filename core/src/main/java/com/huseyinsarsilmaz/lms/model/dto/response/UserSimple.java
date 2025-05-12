package com.huseyinsarsilmaz.lms.model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSimple {

    private long id;
    private String email;
    private String name;
    private String surname;

}