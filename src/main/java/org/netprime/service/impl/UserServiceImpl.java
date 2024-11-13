package org.netprime.service.impl;

import org.netprime.config.JwtUtil;
import org.netprime.dto.LoginRequest;
import org.netprime.dto.UserRequest;
import org.netprime.dto.UserResponse;
import org.netprime.exception.RoleException;
import org.netprime.exception.UserException;
import org.netprime.model.Role;
import org.netprime.model.User;
import org.netprime.repository.RoleRepository;
import org.netprime.repository.UserRepository;
import org.netprime.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, MongoTemplate mongoTemplate, AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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

        //Set<String> rolesNames = savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<SimpleGrantedAuthority> authorities = savedUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
        //Covert User class to Dto
        return new UserResponse(savedUser.getUsername(), savedUser.getName(), savedUser.getEmail(), authorities);
    }

    @Cacheable(value = "users", key = "#username")
    @Override
    public UserResponse findUserByUsername(String username) {
        // Find the user by username
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User not found"));
        //Set<String> rolesNames = user.getRoles().stream()
                //.map(Role::getName).collect(Collectors.toSet());
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
        //Convert user object to dto
        return new UserResponse(user.getUsername(), user.getName(), user.getEmail(), authorities);
    }

    @Override
    public void addRoleToUser(String username, String roleName) {
        // Find the user by username
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User not found"));
        //Find role by its name
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new RoleException("Role not found"));
        // Add the role to the user's role set if it's not already there
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    @Override
    public Map<String, String> loginUser(LoginRequest loginRequest) {
        //Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        System.out.println(userDetails);
        //Generate JWT token
        String token = jwtUtil.generateToken(loginRequest.getEmail(), roles);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User logged in successfully");
        response.put("token", token);
        return response;
    }
}
