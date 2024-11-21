//package com.sarthak.webapp.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sarthak.webapp.model.UserDTO;
//import com.sarthak.webapp.model.UserResponseDTO;
//import com.sarthak.webapp.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(UserController.class)
//class UserControllerTest {
//
//    @Autowired
//    private WebApplicationContext context;
//
//    private MockMvc mockMvc;
//
//    @MockBean
//    private UserService userService;
//
//    @BeforeEach
//    public void setup() {
//        mockMvc = MockMvcBuilders
//                .webAppContextSetup(context)
//                .apply(springSecurity())
//                .build();
//    }
//
//    @Test
//    @WithMockUser
//    void createUser_Success() throws Exception {
//        UserDTO userDTO = new UserDTO();
//        userDTO.setFirstName("John");
//        userDTO.setLastName("Doe");
//        userDTO.setEmail("john@example.com");
//        userDTO.setPassword("password");
//
//        UserResponseDTO responseDTO = new UserResponseDTO();
//        responseDTO.setId(1L);
//        responseDTO.setEmail("john@example.com");
//        responseDTO.setFirstName("John");
//        responseDTO.setLastName("Doe");
//
//        when(userService.createUser(any(UserDTO.class))).thenReturn(responseDTO);
//
//        mockMvc.perform(post("/v1/user")
//                        .with(SecurityMockMvcRequestPostProcessors.csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(new ObjectMapper().writeValueAsString(userDTO)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.email").value("john@example.com"))
//                .andExpect(jsonPath("$.firstName").value("John"))
//                .andExpect(jsonPath("$.lastName").value("Doe"));
//    }
//
//}
package com.sarthak.webapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.webapp.config.SecurityConfig;
import com.sarthak.webapp.model.UserDTO;
import com.sarthak.webapp.model.UserResponseDTO;
import com.sarthak.webapp.service.CustomUserDetailsService;
import com.sarthak.webapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void createUser_Success() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("john@example.com");
        userDTO.setPassword("password123");

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setEmail("john@example.com");
        responseDTO.setFirstName("John");
        responseDTO.setLastName("Doe");
        responseDTO.setAccountCreated("2024-01-01T00:00:00");
        responseDTO.setAccountUpdated("2024-01-01T00:00:00");

        when(userService.createUser(any(UserDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/v1/user")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.accountCreated").exists())
                .andExpect(jsonPath("$.accountUpdated").exists());
    }

    @Test
    void createUser_InvalidData() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/v1/user")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }
}