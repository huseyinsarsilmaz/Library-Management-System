package com.huseyinsarsilmaz.lms.service;

import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.entity.User;

public interface UserService {

    public void isEmailTaken(String email);

    public User register(RegisterRequest req);

    public User getByEmail(String email);

    public User getById(long id);

    public User promote(User user, User.Role newRole);

    public User getFromToken(String token);

    public void checkRole(User user, User.Role requiredRole);

    public User update(User user, UserUpdateRequest req);

    public void delete(User user);

    public void checkActiveById(Long userId);

    public void checkDeactivated(User user);

    public User changeActive(User user, boolean newActive);
}
