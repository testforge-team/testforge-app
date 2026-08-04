package com.testforge.user.controller;

import com.testforge.user.dto.UserDto;
import com.testforge.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints for the "Manage Users" screen.
 *
 * How the protection works, step by step:
 *  1. SecurityConfig says anyRequest().authenticated() -> without a valid JWT
 *     these endpoints return 403 before any code here runs.
 *  2. @PreAuthorize("hasRole('ADMIN')") narrows it further: the JWT's user
 *     must carry ROLE_ADMIN (set in CustomUserDetailsService). A STUDENT
 *     token gets 403 from Spring, handled by GlobalExceptionHandler.
 * So this class contains ZERO security code of its own - one annotation
 * plus Person A's machinery does everything.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/users -> list of all users (ADMIN only). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** GET /api/users/5 -> that one user, or 404 (ADMIN only). */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
