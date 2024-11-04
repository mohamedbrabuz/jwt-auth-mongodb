package org.netprime.service.impl;

import org.netprime.dto.UserRequest;
import org.netprime.dto.UserResponse;
import org.netprime.exception.UserException;
import org.netprime.model.User;
import org.netprime.repository.RoleRepository;
import org.netprime.repository.UserRepository;
import org.netprime.service.UserService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public UserResponse registerUser(UserRequest userRequest) {
        Query queryUsername = new Query();
        queryUsername.addCriteria(Criteria.where("username").is(userRequest.getUsername()));

        Query queryEmail = new Query();
        queryEmail.addCriteria(Criteria.where("email").is(userRequest.getEmail()));
        // Check if user with the same username or email exists
        if (mongoTemplate.exists(queryUsername, User.class)) {
            throw new RuntimeException("Username already exists");
        }
        if (mongoTemplate.exists(queryEmail, User.class)) {
            throw new RuntimeException("Email already exists");
        }
        // End of check

        //Create new user instance
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());

        //Encode the user's password before saving
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        User savedUser = userRepository.save(user);

        //Covert User class to Dto
        return new UserResponse(savedUser.getUsername(), savedUser.getName(), savedUser.getEmail());
    }

    @Override
    public UserResponse findUserByUsername(String username) {
        // Find the user by username
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User not found"));
        //Convert user object to dto
        return new UserResponse(user.getUsername(), user.getName(), user.getEmail());
    }

    @Override
    public void addRoleToUser(String username, String role) {
        // Find the user by username
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User not found"));

    }
}
