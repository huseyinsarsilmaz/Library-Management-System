package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class PassordNotMatchedException extends LmsException {
    public PassordNotMatchedException() {
        super("user.password.not.match", HttpStatus.FORBIDDEN);
    }
}
