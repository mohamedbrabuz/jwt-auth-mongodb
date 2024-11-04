package org.netprime.service;

import org.netprime.dto.UserRequest;
import org.netprime.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRequest userRequest);

    UserResponse findUserByUsername(String username);

    void addRoleToUser(String username, String role);

}