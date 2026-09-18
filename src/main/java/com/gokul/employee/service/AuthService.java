package com.gokul.employee.service;

import com.gokul.employee.dto.AuthResponse;
import com.gokul.employee.dto.LoginRequest;
import com.gokul.employee.dto.RegisterRequest;
import com.gokul.employee.dto.RegisterResponse;
import com.gokul.employee.entity.User;
import com.gokul.employee.exception.EmailAlreadyExistsException;
import com.gokul.employee.exception.InvalidCredentialsException;
import com.gokul.employee.repository.UserRepository;
import com.gokul.employee.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
    }

    public AuthResponse login(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->
                        new InvalidCredentialsException("Invalid email or password"));
        if(!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password");
        }
        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole()
        );
    }
}
