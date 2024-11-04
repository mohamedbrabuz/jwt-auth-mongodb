package org.netprime.controller;

import org.netprime.dto.UserRequest;
import org.netprime.dto.UserResponse;
import org.netprime.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest userRequest) {
        try{
            UserResponse userResponse = userService.registerUser(userRequest);
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<UserResponse> findUserByUsername(@RequestParam("username") String username) {
        try{
            UserResponse userResponse = userService.findUserByUsername(username);
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
