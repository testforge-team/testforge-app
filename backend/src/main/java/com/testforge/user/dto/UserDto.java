package com.testforge.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The SAFE view of a user that we send to the client.
 *
 * Compare with the User entity: the entity has a password field (the BCrypt
 * hash). This DTO deliberately does NOT. Because controllers only ever return
 * DTOs, it becomes impossible to accidentally leak a password hash in JSON.
 * This is the whole reason DTOs exist.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long userId;
    private String name;
    private String email;
    private String role;            // sent as plain text: "STUDENT" or "ADMIN"
    private LocalDateTime createdAt;
}
