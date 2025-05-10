package com.huseyinsarsilmaz.lms.model.dto.response;

import com.huseyinsarsilmaz.lms.model.entity.User;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserDetailed extends UserSimple {

    private boolean isActive;

    public UserDetailed(User user) {
        super(user);
        this.isActive = user.getIsActive();
    }

}