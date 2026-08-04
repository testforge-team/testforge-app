package com.testforge.auth.service;

import com.testforge.auth.dto.AuthResponse;
import com.testforge.auth.dto.LoginRequest;
import com.testforge.auth.dto.RegisterRequest;
import com.testforge.common.enums.Role;
import com.testforge.exception.BadRequestException;
import com.testforge.security.CustomUserDetailsService;
import com.testforge.security.JwtService;
import com.testforge.user.entity.User;
import com.testforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        // 1. Don't allow duplicate emails.
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // 2. Build the user, hashing the password before saving.
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)          // new sign-ups are always students
                .build();
        userRepository.save(user);

        // 3. Log them straight in by issuing a token.
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .name(user.getName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Let Spring verify the email + password. Throws BadCredentialsException if wrong.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // 2. Credentials are valid, so load the user and issue a token.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .name(user.getName())
                .build();
    }
}
