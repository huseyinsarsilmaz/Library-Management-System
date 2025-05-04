package com.huseyinsarsilmaz.lms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lms.model.dto.request.PromoteRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.PromoteResponse;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.service.UserService;
import com.huseyinsarsilmaz.lms.service.Utils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/promote")
    public ResponseEntity<ApiResponse> promote(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PromoteRequest req) {

        User myUser = userService.getUserFromToken(token);
        userService.checkUserRole(myUser, User.Role.ROLE_ADMIN);

        User promotedUser = userService.getByEmail(req.getEmail());
        promotedUser = userService.promote(promotedUser, req.getNewRole());
        return Utils.successResponse(User.class.getSimpleName(), "promoted", new PromoteResponse(promotedUser),
                HttpStatus.OK);
    }

}
