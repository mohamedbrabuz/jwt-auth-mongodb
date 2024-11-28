package org.netprime.controller;

import org.netprime.dto.ApiResponse;
import org.netprime.dto.LoginRequest;
import org.netprime.dto.UserRequest;
import org.netprime.exception.RoleException;
import org.netprime.exception.UserException;
import org.netprime.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserServiceImpl userService;
    private final UserServiceImpl userServiceImpl;

    public UserController(UserServiceImpl userService, UserServiceImpl userServiceImpl) {
        this.userService = userService;
        this.userServiceImpl = userServiceImpl;
    }

    // Endpoint to register a new user
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody UserRequest userRequest) {
        try{
            ApiResponse userResponse = userService.registerUser(userRequest);
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        }catch(Exception e){
            throw new UserException(e.getMessage());
        }
    }

    // Endpoint to find a user by username
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> findUserByUsername(@RequestParam("username") String username) {
        try{
            ApiResponse userResponse = userService.findUserByUsername(username);
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        }catch(Exception e){
            throw new UserException(e.getMessage());
        }
    }

    // Endpoint to assign a role to a user
    @PostMapping("/assign-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> assignRoleToUser(@RequestParam String username, @RequestParam String roleName) {
        try {
            return new ResponseEntity<>(userService.addRoleToUser(username, roleName), HttpStatus.OK);
        }catch(Exception e){
            throw new RoleException(e.getMessage());
        }
    }

    // Endpoint for user login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        try{
            return new ResponseEntity<>(userService.loginUser(loginRequest), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Endpoint for user logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser(@RequestHeader("Authorization") String token) {
        try{
            return new ResponseEntity<>(userServiceImpl.logoutUser(token), HttpStatus.OK);
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
