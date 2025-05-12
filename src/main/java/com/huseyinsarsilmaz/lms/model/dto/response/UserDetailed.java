package com.huseyinsarsilmaz.lms.model.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserDetailed extends UserSimple {

    private Boolean isActive;
}