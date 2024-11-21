package com.sarthak.webapp.service;

import com.sarthak.webapp.model.User;
import com.sarthak.webapp.model.UserDTO;
import com.sarthak.webapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserVerificationService verificationService;

    @Mock
    private MetricsService metricsService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(
                userRepository,
                passwordEncoder,
                verificationService,
                metricsService
        );
    }

    @Test
    void createUser_Success() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setPassword("password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        doNothing().when(verificationService).sendVerificationEmail(any(User.class));

        // Act
        var result = userService.createUser(userDTO);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        verify(userRepository).save(any(User.class));
        verify(verificationService).sendVerificationEmail(any(User.class));
        verify(metricsService).incrementUserCreations();
    }

    @Test
    void createUser_ExistingEmail_ThrowsException() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("existing@example.com");
        userDTO.setPassword("password123");

        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any(User.class));
        verify(verificationService, never()).sendVerificationEmail(any(User.class));
    }

    @Test
    void createUser_InvalidPassword_ThrowsException() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("short"); // Too short password

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any(User.class));
        verify(verificationService, never()).sendVerificationEmail(any(User.class));
    }

    @Test
    void getUserByEmail_Success() {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        var result = userService.getUserByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
    }

    @Test
    void getUserByEmail_NotFound_ThrowsException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> userService.getUserByEmail(email));
    }

    @Test
    void updateUser_Success() {
        // Arrange
        String email = "test@example.com";
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail(email);
        updateDTO.setFirstName("UpdatedFirst");
        updateDTO.setLastName("UpdatedLast");
        updateDTO.setPassword("newpassword123");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setFirstName("OldFirst");
        existingUser.setLastName("OldLast");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = userService.updateUser(updateDTO, email);

        // Assert
        assertNotNull(result);
        assertEquals("UpdatedFirst", result.getFirstName());
        assertEquals("UpdatedLast", result.getLastName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_EmailChange_ThrowsException() {
        // Arrange
        String currentEmail = "current@example.com";
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("different@example.com");

        User existingUser = new User();
        existingUser.setEmail(currentEmail);

        when(userRepository.findByEmail(currentEmail)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> userService.updateUser(updateDTO, currentEmail));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void isUserVerified_ReturnsTrueForVerifiedUser() {
        // Arrange
        String email = "verified@example.com";
        User user = new User();
        user.setVerified(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        boolean result = userService.isUserVerified(email);

        // Assert
        assertTrue(result);
    }

    @Test
    void isUserVerified_ReturnsFalseForUnverifiedUser() {
        // Arrange
        String email = "unverified@example.com";
        User user = new User();
        user.setVerified(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        boolean result = userService.isUserVerified(email);

        // Assert
        assertFalse(result);
    }
}