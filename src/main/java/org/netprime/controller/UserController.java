package org.netprime.controller;

import org.netprime.dto.LoginRequest;
import org.netprime.dto.UserRequest;
import org.netprime.dto.UserResponse;
import org.netprime.exception.RoleException;
import org.netprime.exception.UserException;
import org.netprime.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest userRequest) {
        try{
            UserResponse userResponse = userService.registerUser(userRequest);
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        }catch(Exception e){
            throw new UserException(e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<UserResponse> findUserByUsername(@RequestParam("username") String username) {
        try{
            UserResponse userResponse = userService.findUserByUsername(username);
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        }catch(Exception e){
            throw new UserException(e.getMessage());
        }
    }

    @PostMapping("/assign-role")
    public ResponseEntity<String> assignRoleToUser(@RequestParam String username, @RequestParam String roleName) {
        try {
            userService.addRoleToUser(username, roleName);
            return new ResponseEntity<>("Role assigned", HttpStatus.OK);
        }catch(Exception e){
            throw new RoleException(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody LoginRequest loginRequest) {
        try{
            Map<String, String> response = userService.loginUser(loginRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
