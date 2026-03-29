package com.example.expensetracker.unit.controller;

import com.example.expensetracker.config.PasswordConfig;
import com.example.expensetracker.controller.AuthController;
import com.example.expensetracker.model.User;
import com.example.expensetracker.security.JwtService;
import com.example.expensetracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  
@Import(PasswordConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("Иван Иванов")
                .email("ivan@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.Role.NORMAL)
                .build();
    }

    @Test
    void register_ShouldReturn201_WhenRegistrationIsSuccessful() throws Exception {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest(
                "Иван Иванов", "ivan@example.com", "password123", null, null
        );

        when(userService.register(any(), any(), any(), any(), any())).thenReturn(testUser);
        when(jwtService.generateToken(any())).thenReturn("test-jwt-token");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.user.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.token").value("test-jwt-token"));
    }

    @Test
    void register_ShouldReturn400_WhenEmailIsAlreadyTaken() throws Exception {
        AuthController.RegisterRequest request = new AuthController.RegisterRequest(
                "Иван Иванов", "ivan@example.com", "password123", null, null
        );

        when(userService.register(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Электронная почта уже используется"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messege").value("Электронная почта уже используется"));
    }

    @Test
    void login_ShouldReturn201_WhenCredentialsAreValid() throws Exception {
        AuthController.LoginRequest request = new AuthController.LoginRequest(
                "ivan@example.com", "password123"
        );

        when(userService.findByEmail("ivan@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(1L)).thenReturn("test-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.user.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.token").value("test-jwt-token"));
    }

    @Test
    void login_ShouldReturn400_WhenCredentialsAreInvalid() throws Exception {
        AuthController.LoginRequest request = new AuthController.LoginRequest(
                "ivan@example.com", "wrongpassword"
        );

        when(userService.findByEmail("ivan@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messege").value("Неверные учетные данные"));
    }
   
}