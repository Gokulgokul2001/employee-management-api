package com.gokul.employee.service;

import com.gokul.employee.dto.AuthResponse;
import com.gokul.employee.dto.RegisterResponse;
import com.gokul.employee.dto.LoginRequest;
import com.gokul.employee.exception.EmailAlreadyExistsException;
import com.gokul.employee.exception.InvalidCredentialsException;
import com.gokul.employee.repository.UserRepository;
import com.gokul.employee.security.JwtService;
import com.gokul.employee.dto.RegisterRequest;
import com.gokul.employee.entity.Role;
import com.gokul.employee.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void register_shouldCreateUserSuccessfully() {

        RegisterRequest request = new RegisterRequest(
                "Gokul",
                "gokul@test.com",
                "password123",
                Role.EMPLOYEE
        );

        User savedUser = User.builder()
                .id(1L)
                .name("Gokul")
                .email("gokul@test.com")
                .password("encodedPassword")
                .role(Role.EMPLOYEE)
                .build();

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Gokul", response.getName());
        assertEquals("gokul@test.com", response.getEmail());
        assertEquals(Role.EMPLOYEE, response.getRole());

        verify(userRepository).existsByEmail("gokul@test.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest(
                "Gokul",
                "gokul@test.com",
                "password123",
                Role.EMPLOYEE
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        EmailAlreadyExistsException exception =
                assertThrows(
                        EmailAlreadyExistsException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Email is already registered",
                exception.getMessage()
        );

        verify(userRepository).existsByEmail("gokul@test.com");

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }
    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        LoginRequest request = new LoginRequest(
                "gokul@test.com",
                "password123"
        );

        User user = User.builder()
                .id(1L)
                .name("Gokul")
                .email("gokul@test.com")
                .password("encodedPassword")
                .role(Role.EMPLOYEE)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )).thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("gokul@test.com", response.getEmail());
        assertEquals(Role.EMPLOYEE, response.getRole());

        verify(userRepository).findByEmail("gokul@test.com");
        verify(passwordEncoder).matches(
                "password123",
                "encodedPassword"
        );
        verify(jwtService).generateToken(user);
    }
    @Test
    void login_shouldThrowException_whenPasswordIsInvalid() {

        LoginRequest request = new LoginRequest(
                "gokul@test.com",
                "wrongPassword"
        );

        User user = User.builder()
                .id(1L)
                .name("Gokul")
                .email("gokul@test.com")
                .password("encodedPassword")
                .role(Role.EMPLOYEE)
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )).thenReturn(false);

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("gokul@test.com");

        verify(passwordEncoder).matches(
                "wrongPassword",
                "encodedPassword"
        );

        verify(jwtService, never())
                .generateToken(any(User.class));
    }
    @Test
    void login_shouldThrowException_whenEmailDoesNotExist() {

        LoginRequest request = new LoginRequest(
                "unknown@test.com",
                "password123"
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("unknown@test.com");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(any(User.class));
    }
}