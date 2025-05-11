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
import com.huseyinsarsilmaz.lms.model.dto.response.UserDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.UserSimple;
import com.huseyinsarsilmaz.lms.model.entity.User;
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

    private User authorizeModifyAccessToUser(String token, long targetUserId, User.Role baseRole) {
        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, baseRole);

        User targetUser = userService.getById(targetUserId);
        String roles = targetUser.getRoles();

        boolean isManager = roles.contains(User.Role.ROLE_ADMIN.name()) ||
                roles.contains(User.Role.ROLE_LIBRARIAN.name());

        if (isManager) {
            userService.checkRole(myUser, User.Role.ROLE_ADMIN);
        }

        return targetUser;
    }

    private User authorizeReadAccessToUser(String token, long targetUserId, User.Role baseRole) {
        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, baseRole);

        User targetUser = userService.getById(targetUserId);
        String roles = targetUser.getRoles();

        boolean isAdmin = roles.contains(User.Role.ROLE_ADMIN.name());

        if (isAdmin) {
            userService.checkRole(myUser, User.Role.ROLE_ADMIN);
        }

        return targetUser;
    }

    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<PromoteResponse>> promote(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PromoteRequest req) {

        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, User.Role.ROLE_ADMIN);

        User promotedUser = userService.getByEmail(req.getEmail());
        promotedUser = userService.promote(promotedUser, req.getNewRole());
        return responseBuilder.success(User.class.getSimpleName(), "promoted", new PromoteResponse(promotedUser),
                HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSimple>> getMyUser(@RequestHeader("Authorization") String token) {

        User myUser = userService.getFromToken(token);

        return responseBuilder.success("User", "acquired", new UserSimple(myUser), HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserSimple>> updateMyUser(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserUpdateRequest req) {

        User myUser = userService.getFromToken(token);
        if (!myUser.getEmail().equals(req.getEmail())) {
            userService.isEmailTaken(req.getEmail());
        }

        myUser = userService.update(myUser, req);

        return responseBuilder.success("User Profile", "updated", new UserSimple(myUser), HttpStatus.OK);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<UserSimple>> deleteMyUser(@RequestHeader("Authorization") String token) {

        User myUser = userService.getFromToken(token);

        userService.delete(myUser);

        return responseBuilder.success("User", "deleted", new UserSimple(myUser), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSimple>> getUser(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") long id) {

        User targetUser = authorizeReadAccessToUser(token, id, User.Role.ROLE_LIBRARIAN);

        return responseBuilder.success("User", "acquired", new UserSimple(targetUser), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSimple>> updateUser(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserUpdateRequest req,
            @PathVariable("id") long id) {

        User targetUser = authorizeModifyAccessToUser(token, id, User.Role.ROLE_LIBRARIAN);

        if (!targetUser.getEmail().equals(req.getEmail())) {
            userService.isEmailTaken(req.getEmail());
        }

        targetUser = userService.update(targetUser, req);

        return responseBuilder.success("User Profile", "updated", new UserSimple(targetUser), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSimple>> deleteUser(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") long id) {

        User targetUser = authorizeModifyAccessToUser(token, id, User.Role.ROLE_LIBRARIAN);

        userService.delete(targetUser);

        return responseBuilder.success("User", "acquired", new UserSimple(targetUser), HttpStatus.OK);
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<UserDetailed>> excuseBorrowing(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") long id) {

        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);

        User reactivatedUser = userService.getById(id);
        userService.checkDeactivated(reactivatedUser);

        reactivatedUser = userService.changeActive(reactivatedUser, true);
        borrowingService.excuseReturnedOverdueBorrowings(reactivatedUser);

        return responseBuilder.success(User.class.getSimpleName(), "re-activated", new UserDetailed(reactivatedUser),
                HttpStatus.OK);
    }
}
