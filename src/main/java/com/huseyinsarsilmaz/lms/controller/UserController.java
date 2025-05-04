package com.huseyinsarsilmaz.lms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lms.model.dto.request.PromoteRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.PromoteResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.UserSimple;
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

        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, User.Role.ROLE_ADMIN);

        User promotedUser = userService.getByEmail(req.getEmail());
        promotedUser = userService.promote(promotedUser, req.getNewRole());
        return Utils.successResponse(User.class.getSimpleName(), "promoted", new PromoteResponse(promotedUser),
                HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyUser(@RequestHeader("Authorization") String token) {

        User myUser = userService.getFromToken(token);

        return Utils.successResponse("User", "acquired", new UserSimple(myUser), HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse> updateMyUser(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserUpdateRequest req) {

        User myUser = userService.getFromToken(token);
        if (!myUser.getEmail().equals(req.getEmail())) {
            userService.isEmailTaken(req.getEmail());
        }

        myUser = userService.update(myUser, req);

        return Utils.successResponse("User Profile", "updated", new UserSimple(myUser), HttpStatus.OK);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse> deleteMyUser(@RequestHeader("Authorization") String token) {

        User myUser = userService.getFromToken(token);

        userService.deleteUser(myUser);

        return Utils.successResponse("User", "deleted", new UserSimple(myUser), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUser(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") long id) {

        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);

        User askedUser = userService.getById(id);

        if (askedUser.getRoles().contains(User.Role.ROLE_ADMIN.name())) {
            userService.checkRole(myUser, User.Role.ROLE_ADMIN);
        }

        return Utils.successResponse("User", "acquired", new UserSimple(askedUser), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserUpdateRequest req,
            @PathVariable("id") long id) {

        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);
        User askedUser = userService.getById(id);

        if (askedUser.getRoles().contains(User.Role.ROLE_ADMIN.name())) {
            userService.checkRole(myUser, User.Role.ROLE_ADMIN);
        }

        if (!askedUser.getEmail().equals(req.getEmail())) {
            userService.isEmailTaken(req.getEmail());
        }

        askedUser = userService.update(askedUser, req);

        return Utils.successResponse("User Profile", "updated", new UserSimple(askedUser), HttpStatus.OK);
    }
}
