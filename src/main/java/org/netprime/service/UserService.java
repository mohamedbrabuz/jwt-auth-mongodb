package org.netprime.service;

import org.netprime.dto.ApiResponse;
import org.netprime.dto.LoginRequest;
import org.netprime.dto.UserRequest;

import java.util.Map;

public interface UserService {

    ApiResponse registerUser(UserRequest userRequest);

    ApiResponse findUserByUsername(String username);

    void addRoleToUser(String username, String role);

    Map<String, String> loginUser(LoginRequest loginRequest);
}