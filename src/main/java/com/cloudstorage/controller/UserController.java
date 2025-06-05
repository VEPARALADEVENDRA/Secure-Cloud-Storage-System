package com.cloudstorage.controller;

import com.cloudstorage.model.User;
import com.cloudstorage.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Api(tags = "User Management", description = "Endpoints for managing users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/email/{email}")
    @ApiOperation(value = "Get User by Email", notes = "Retrieves a user's details based on their email address.")
    public ResponseEntity<User> getUserByEmail(
            @ApiParam(value = "Email address of the user to retrieve", required = true) @PathVariable String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{userId}")
    @ApiOperation(value = "Get User by ID", notes = "Retrieves a user's details based on their ID.")
    public ResponseEntity<User> getUserById(
            @ApiParam(value = "ID of the user to retrieve", required = true) @PathVariable Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
