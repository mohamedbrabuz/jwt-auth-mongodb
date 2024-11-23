package org.netprime.service.impl;

import org.netprime.config.JwtUtil;
import org.netprime.dto.ApiResponse;
import org.netprime.dto.LoginRequest;
import org.netprime.dto.UserRequest;
import org.netprime.exception.RoleException;
import org.netprime.exception.UserException;
import org.netprime.model.Role;
import org.netprime.model.User;
import org.netprime.repository.RoleRepository;
import org.netprime.repository.UserRepository;
import org.netprime.service.UserService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.concurrent.TimeUnit;
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
    private final RedisTemplate<String, String> redisTemplate;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, MongoTemplate mongoTemplate, AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService, RedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ApiResponse registerUser(UserRequest userRequest) {
        // Check if user with the same username or email exists
        if (isUserExists(userRequest.getUsername(), userRequest.getEmail())) {
            throw new RuntimeException("Username or E;qil already exists");
        }

        //Create new user instance
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());

        //Encode the user's password before saving
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        userRepository.save(user);

        //Return the response
        return new ApiResponse(true, "User has been created");
    }

    @Override
    public ApiResponse findUserByUsername(String username) {
        // Find the user by username
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User not found"));

        //Retrieve the roles related to user
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        //Return the response
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("username", user.getUsername());
        extraData.put("email", user.getEmail());
        extraData.put("authorities", authorities);
        return new ApiResponse(true, "User found", extraData);
    }

    @Override
    public void addRoleToUser(String username, String roleName) {
        // Find the user by username
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserException("User not found"));
        //Find role by its name
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new RoleException("Role not found"));
        // Add the role to the user's role Set if it's not already there
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    @Override
    public ApiResponse loginUser(LoginRequest loginRequest) {
        //Redis key for storing the token
        String redisKey = "JWT:" + loginRequest.getEmail();

        //Check for an existing token in Redis
        String existingToken = redisTemplate.opsForValue().get(redisKey);
        if (existingToken != null && jwtUtil.validateToken(existingToken, loginRequest.getEmail())) {
            return new ApiResponse(true, "Token retrieved from cache");
        }

        //Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

        // Extract the roles from authenticate user
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        //Generate JWT token
        String token = jwtUtil.generateToken(loginRequest.getEmail(), roles);

        //Store the token in redis
        redisTemplate.opsForValue().set(redisKey, token, jwtUtil.getExpiration(), TimeUnit.MILLISECONDS);

        // Return the response
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("token", token);
        return new ApiResponse(true, "User logged in successfully", extraData);
    }


    // Method to verify if the user exists in the DB
    private boolean isUserExists(String username, String email) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("username").is(username),
                Criteria.where("email").is(email)
        ));
        return mongoTemplate.exists(query, User.class);
    }
}
