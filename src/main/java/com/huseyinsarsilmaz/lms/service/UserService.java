package com.huseyinsarsilmaz.lms.service;

import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.entity.User;

public interface UserService {

    public void isEmailTaken(String email);

    public User register(RegisterRequest req);

    public User getByEmail(String email);

    public User promote(User user, User.Role newRole);

    public User getUserFromToken(String token);
}
