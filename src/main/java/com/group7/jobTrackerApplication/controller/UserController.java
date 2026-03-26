package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.DTO.UpdateUserRoleRequest;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

/**
 * Exposes admin-only endpoints for viewing and managing user accounts.
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Users", description = "Admin-only operations for user management")
public class UserController {

    private final UserService userService;

    /**
     * Creates a new admin user controller.
     *
     * @param adminService service responsible for user administration
     */
    public UserController(UserService adminService){
        this.userService = adminService;
    }

    @GetMapping
    @Operation(summary = "List all users", description = "Returns all users in the system. Admin role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a user", description = "Returns a specific user by id. Admin role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin access required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> getUserById(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Update a user role", description = "Changes the role of a specific user. Admin role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin access required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRoleRequest request){
        User updated = userService.update(userId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user", description = "Deletes a specific user and associated data. Admin role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Admin access required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId){
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
