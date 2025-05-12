package com.huseyinsarsilmaz.lms.service;

import com.huseyinsarsilmaz.lms.model.dto.request.LoginRequest;

public interface AuthService {

    public String authenticateAndGenerateToken(LoginRequest request);
}
