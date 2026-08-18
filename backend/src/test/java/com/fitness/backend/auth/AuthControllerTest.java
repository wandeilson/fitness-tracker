package com.fitness.backend.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fitness.backend.auth.dto.AuthResponse;
import com.fitness.backend.auth.dto.LoginRequest;
import com.fitness.backend.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void registerShouldReturn201WhenPayloadIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("user@test.com", "123456", "User Test");
        AuthResponse response = new AuthResponse("jwt-token", "user@test.com", "User Test");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void registerShouldReturn400WhenPayloadIsInvalid() throws Exception {
        String invalidPayload = """
            {
              \"email\": \"not-an-email\",
              \"password\": \"123\",
              \"fullName\": \"\"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid request data"));
    }

    @Test
    void loginShouldReturn200WhenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "123456");
        AuthResponse response = new AuthResponse("jwt-token", "user@test.com", "User Test");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void loginShouldReturn401WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "wrong");
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
