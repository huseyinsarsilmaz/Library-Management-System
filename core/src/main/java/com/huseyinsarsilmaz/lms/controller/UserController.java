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

import com.huseyinsarsilmaz.lms.model.dto.request.PasswordUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.PromoteRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.LmsApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.PromoteResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.UserDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.UserSimple;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.model.mapper.UserMapper;
import com.huseyinsarsilmaz.lms.security.CurrentUser;
import com.huseyinsarsilmaz.lms.service.BorrowingService;
import com.huseyinsarsilmaz.lms.service.UserService;
import com.huseyinsarsilmaz.lms.util.LmsResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BorrowingService borrowingService;
    private final LmsResponseBuilder responseBuilder;
    private final UserMapper userMapper;

    private User authorizeAccessToUser(User myUser, long targetUserId, boolean modify) {
        userService.checkHasRole(myUser, User.Role.ROLE_LIBRARIAN);

        User targetUser = userService.getById(targetUserId);
        String roles = targetUser.getRoles();

        boolean isAdmin = roles.contains(User.Role.ROLE_ADMIN.name());
        boolean isManager = isAdmin ||
                roles.contains(User.Role.ROLE_LIBRARIAN.name());

        if ((isManager && modify) || isAdmin) {
            userService.checkHasRole(myUser, User.Role.ROLE_ADMIN);
        }

        return targetUser;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/promote")
    @Operation(summary = "Promote a user", description = "Promotes a user to a higher role ( from PATRON to LIBRARIAN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully promoted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid role or email", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided email", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions to promote user", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<PromoteResponse>> promote(@Valid @RequestBody PromoteRequest req) {
        User promotedUser = userService.getByEmail(req.getEmail());
        promotedUser = userService.promote(promotedUser, req.getNewRole());

        return responseBuilder.success("User", "promoted", new PromoteResponse(promotedUser), HttpStatus.OK);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Fetches the details of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details fetched", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSimple.class)))
    })
    public ResponseEntity<LmsApiResponse<UserSimple>> getMyUser(@CurrentUser User myUser) {

        return responseBuilder.success("User", "fetched", userMapper.toDtoSimple(myUser), HttpStatus.OK);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates the details of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSimple.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<UserSimple>> updateMyUser(@CurrentUser User myUser,
            @Valid @RequestBody UserUpdateRequest req) {
        if (!myUser.getEmail().equals(req.getEmail())) {
            userService.isEmailTaken(req.getEmail());
        }

        myUser = userService.update(myUser, req);

        return responseBuilder.success("User Profile", "updated", userMapper.toDtoSimple(myUser), HttpStatus.OK);
    }

    @PutMapping("/me/password")
    @Operation(summary = "Update current user password", description = "Updates the password for the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User password successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSimple.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid password format or mismatch", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<UserSimple>> updateMyPassword(@CurrentUser User myUser,
            @Valid @RequestBody PasswordUpdateRequest req) {

        myUser = userService.updatePassword(myUser, req);

        return responseBuilder.success("User Password", "updated", userMapper.toDtoSimple(myUser), HttpStatus.OK);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current user", description = "Deletes the account of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User account successfully deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSimple.class)))
    })
    public ResponseEntity<LmsApiResponse<UserSimple>> deleteMyUser(@CurrentUser User myUser) {
        userService.delete(myUser);

        return responseBuilder.success("User", "deleted", userMapper.toDtoSimple(myUser), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Fetches a user by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details fetched", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSimple.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<UserSimple>> getUser(
            @CurrentUser User myUser,
            @PathVariable("id") long id) {
        User targetUser = authorizeAccessToUser(myUser, id, false);

        return responseBuilder.success("User", "fetched", userMapper.toDtoSimple(targetUser), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user by ID", description = "Updates the profile of a user by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSimple.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<UserSimple>> updateUser(
            @CurrentUser User myUser,
            @Valid @RequestBody UserUpdateRequest req, @PathVariable("id") long id) {
        User targetUser = authorizeAccessToUser(myUser, id, true);

        if (!targetUser.getEmail().equals(req.getEmail())) {
            userService.isEmailTaken(req.getEmail());
        }

        targetUser = userService.update(targetUser, req);

        return responseBuilder.success("User Profile", "updated", userMapper.toDtoSimple(targetUser), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by ID", description = "Deletes a user by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSimple.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<UserSimple>> deleteUser(
            @CurrentUser User myUser,
            @PathVariable("id") long id) {
        User targetUser = authorizeAccessToUser(myUser, id, true);
        userService.delete(targetUser);

        return responseBuilder.success("User", "deleted", userMapper.toDtoSimple(targetUser), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate user", description = "Reactivates a user account that has been deactivated. Only librarians can reactivate users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully reactivated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDetailed.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - User not found with the provided ID", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - User is not deactivated", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<UserDetailed>> reactivateUser(@CurrentUser User myUser,
            @PathVariable("id") long id) {
        User reactivatedUser = userService.getById(id);
        userService.checkDeactivated(reactivatedUser);

        reactivatedUser = userService.changeActive(reactivatedUser, true);
        borrowingService.excuseReturnedOverdueBorrowings(reactivatedUser);

        return responseBuilder.success("User", "re-activated", userMapper.toDtoDetailed(reactivatedUser),
                HttpStatus.OK);
    }
}

