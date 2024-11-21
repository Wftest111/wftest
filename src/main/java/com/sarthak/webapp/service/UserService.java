package com.sarthak.webapp.service;

import com.sarthak.webapp.model.User;
import com.sarthak.webapp.model.UserDTO;
import com.sarthak.webapp.model.UserResponseDTO;
import com.sarthak.webapp.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserVerificationService verificationService;
    private final MetricsService metricsService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserVerificationService verificationService,
                       MetricsService metricsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.metricsService = metricsService;
    }

    @Transactional
    @Timed(value = "user.creation.time", description = "Time taken to create new user")
    public UserResponseDTO createUser(UserDTO userDTO) {
        logger.info("Attempting to create new user with email: {}", userDTO.getEmail());
        long startTime = System.currentTimeMillis();

        try {
            // Check if user already exists
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                logger.warn("User creation failed: Email already exists: {}", userDTO.getEmail());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with this email already exists");
            }

            // Validate password requirements
            validatePassword(userDTO.getPassword());

            // Create new user
            User user = new User();
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setVerified(false);
            user.setAccountCreated(LocalDateTime.now());

            // Save user
            User savedUser = userRepository.save(user);
            logger.info("User created successfully: {}", savedUser.getEmail());

            // Send verification email
            verificationService.sendVerificationEmail(savedUser);
            logger.info("Verification email sent to: {}", savedUser.getEmail());

            // Record metrics
            metricsService.incrementUserCreations();
            metricsService.recordDbOperationTime(System.currentTimeMillis() - startTime);

            return mapToResponseDTO(savedUser);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while creating user: {}", userDTO.getEmail(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user: " + e.getMessage());
        }
    }

    @Timed(value = "user.get.time", description = "Time taken to retrieve user")
    public UserResponseDTO getUserByEmail(String email) {
        logger.info("Retrieving user information for email: {}", email);
        try {
            User user = getUserEntityByEmail(email);
            return mapToResponseDTO(user);
        } catch (Exception e) {
            logger.error("Error retrieving user with email: {}", email, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @Timed(value = "user.update.time", description = "Time taken to update user")
    @Transactional
    public UserResponseDTO updateUser(UserDTO userDTO, String currentUserEmail) {
        logger.info("Attempting to update user: {}", currentUserEmail);
        long startTime = System.currentTimeMillis();

        try {
            User user = getUserEntityByEmail(currentUserEmail);

            // Validate update request
            if (!currentUserEmail.equals(userDTO.getEmail())) {
                logger.warn("Update attempt failed: Email cannot be changed for user: {}", currentUserEmail);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be changed");
            }

            // Update allowed fields
            if (userDTO.getFirstName() != null) {
                user.setFirstName(userDTO.getFirstName());
            }
            if (userDTO.getLastName() != null) {
                user.setLastName(userDTO.getLastName());
            }
            if (userDTO.getPassword() != null) {
                validatePassword(userDTO.getPassword());
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }

            user.setAccountUpdated(LocalDateTime.now());
            User updatedUser = userRepository.save(user);

            // Record metrics
            metricsService.recordDbOperationTime(System.currentTimeMillis() - startTime);

            logger.info("User updated successfully: {}", updatedUser.getEmail());
            return mapToResponseDTO(updatedUser);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user: {}", currentUserEmail, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user: " + e.getMessage());
        }
    }

    @Timed(value = "db.query.getUserEntity", description = "Time taken to fetch user entity")
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
    }

    public boolean isUserVerified(String email) {
        logger.debug("Checking verification status for user: {}", email);
        return userRepository.findByEmail(email)
                .map(User::isVerified)
                .orElse(false);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            logger.warn("Password validation failed: Password must be at least 8 characters long");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long");
        }
        // Add more password validation rules if needed
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(user.getId());
        responseDTO.setFirstName(user.getFirstName());
        responseDTO.setLastName(user.getLastName());
        responseDTO.setEmail(user.getEmail());
        responseDTO.setAccountCreated(user.getAccountCreated() != null ?
                user.getAccountCreated().toString() : null);
        responseDTO.setAccountUpdated(user.getAccountUpdated() != null ?
                user.getAccountUpdated().toString() : null);
        return responseDTO;
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}