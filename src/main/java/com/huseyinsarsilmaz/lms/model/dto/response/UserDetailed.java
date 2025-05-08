package com.huseyinsarsilmaz.lms.model.dto.response;

import com.huseyinsarsilmaz.lms.model.entity.User;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailed extends UserSimple {

    private final boolean isActive;

    public UserDetailed(User user) {
        super(user);
        this.isActive = user.getIsActive();
    }

}