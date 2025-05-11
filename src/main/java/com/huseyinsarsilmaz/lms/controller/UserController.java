package com.huseyinsarsilmaz.lms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lms.model.dto.request.PromoteRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.PromoteResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.UserDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.UserSimple;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.security.CurrentUser;
import com.huseyinsarsilmaz.lms.service.BorrowingService;
import com.huseyinsarsilmaz.lms.service.UserService;
import com.huseyinsarsilmaz.lms.util.ResponseBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BorrowingService borrowingService;
    private final ResponseBuilder responseBuilder;

    private User authorizeAccessToUser(User myUser, long targetUserId, boolean modify) {
        userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);

        User targetUser = userService.getById(targetUserId);
        String roles = targetUser.getRoles();

        boolean isAdmin = roles.contains(User.Role.ROLE_ADMIN.name());
        boolean isManager = isAdmin ||
                roles.contains(User.Role.ROLE_LIBRARIAN.name());

        if ((isManager && modify) || isAdmin) {
            userService.checkRole(myUser, User.Role.ROLE_ADMIN);
        }

        return targetUser;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<PromoteResponse>> promote(@Valid @RequestBody PromoteRequest req) {
        User promotedUser = userService.getByEmail(req.getEmail());
        promotedUser = userService.promote(promotedUser, req.getNewRole());

        return responseBuilder.success("User", "promoted", new PromoteResponse(promotedUser), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSimple>> getMyUser(@CurrentUser User myUser) {

        return responseBuilder.success("User", "fetched", new UserSimple(myUser), HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserSimple>> updateMyUser(@CurrentUser User myUser,
            @Valid @RequestBody UserUpdateRequest req) {
        if (!myUser.getEmail().equals(req.getEmail())) {
            userService.isEmailTaken(req.getEmail());
        }

        myUser = userService.update(myUser, req);

        return responseBuilder.success("User Profile", "updated", new UserSimple(myUser), HttpStatus.OK);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<UserSimple>> deleteMyUser(@CurrentUser User myUser) {
        userService.delete(myUser);

        return responseBuilder.success("User", "deleted", new UserSimple(myUser), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSimple>> getUser(
            @CurrentUser User myUser,
            @PathVariable("id") long id) {
        User targetUser = authorizeAccessToUser(myUser, id, false);

        return responseBuilder.success("User", "fetched", new UserSimple(targetUser), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSimple>> updateUser(
            @CurrentUser User myUser,
            @Valid @RequestBody UserUpdateRequest req, @PathVariable("id") long id) {
        User targetUser = authorizeAccessToUser(myUser, id, true);

        if (!targetUser.getEmail().equals(req.getEmail())) {
            userService.isEmailTaken(req.getEmail());
        }

        targetUser = userService.update(targetUser, req);

        return responseBuilder.success("User Profile", "updated", new UserSimple(targetUser), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSimple>> deleteUser(
            @CurrentUser User myUser,
            @PathVariable("id") long id) {
        User targetUser = authorizeAccessToUser(myUser, id, true);
        userService.delete(targetUser);

        return responseBuilder.success("User", "deleted", new UserSimple(targetUser), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<UserDetailed>> reactivateUser(@CurrentUser User myUser,
            @PathVariable("id") long id) {
        User reactivatedUser = userService.getById(id);
        userService.checkDeactivated(reactivatedUser);

        reactivatedUser = userService.changeActive(reactivatedUser, true);
        borrowingService.excuseReturnedOverdueBorrowings(reactivatedUser);

        return responseBuilder.success("User", "re-activated", new UserDetailed(reactivatedUser), HttpStatus.OK);
    }
}

