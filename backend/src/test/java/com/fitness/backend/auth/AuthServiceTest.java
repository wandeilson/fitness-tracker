package com.fitness.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitness.backend.auth.dto.AuthResponse;
import com.fitness.backend.auth.dto.LoginRequest;
import com.fitness.backend.auth.dto.RegisterRequest;
import com.fitness.backend.user.User;
import com.fitness.backend.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@test.com");
        user.setFullName("User Test");
        user.setPasswordHash("hash");
    }

    @Test
    void registerShouldCreateUserAndReturnTokenWhenEmailIsNew() {
        RegisterRequest request = new RegisterRequest("user@test.com", "123456", "User Test");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.token());
        assertEquals("user@test.com", response.email());
        assertEquals("User Test", response.fullName());
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("user@test.com", "123456", "User Test");
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        assertEquals("Email already registered", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("user@test.com", "123456");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("jwt-token", response.token());
        assertEquals("user@test.com", response.email());
        assertEquals("User Test", response.fullName());
    }

    @Test
    void loginShouldThrowWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("user@test.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void loginShouldThrowWhenUserIsNotFoundAfterAuthentication() {
        LoginRequest request = new LoginRequest("user@test.com", "123456");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.login(request));

        assertEquals("Invalid credentials", ex.getMessage());
    }
}
