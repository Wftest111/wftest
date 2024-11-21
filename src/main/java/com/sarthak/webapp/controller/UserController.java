package com.sarthak.webapp.controller;

import com.sarthak.webapp.model.UserDTO;
import com.sarthak.webapp.model.UserResponseDTO;
import com.sarthak.webapp.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        logger.info("Received user creation request for email: {}", userDTO.getEmail());
        try {
            UserResponseDTO userResponse = userService.createUser(userDTO);
            logger.info("Successfully created user with email: {}", userResponse.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (ResponseStatusException e) {
            logger.error("Failed to create user with email: {}. Reason: {}", userDTO.getEmail(), e.getReason());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while creating user with email: {}. Error: {}", userDTO.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error creating user: " + e.getMessage());
        }
    }

    @GetMapping("/self")
    public ResponseEntity<UserResponseDTO> getUserInfo(Principal principal) {
        logger.info("Retrieving user info for: {}", principal.getName());
        try {
            UserResponseDTO user = userService.getUserByEmail(principal.getName());
            logger.info("Successfully retrieved user info for: {}", principal.getName());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Failed to retrieve user info for: {}. Error: {}", principal.getName(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/self")
    public ResponseEntity<Void> updateUserInfo(@Valid @RequestBody UserDTO userDTO, Principal principal) {
        userService.updateUser(userDTO, principal.getName());
        return ResponseEntity.noContent().build();
    }
}