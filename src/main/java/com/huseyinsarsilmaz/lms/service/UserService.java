package com.huseyinsarsilmaz.lms.service;

import org.springframework.stereotype.Service;

import com.huseyinsarsilmaz.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

}
