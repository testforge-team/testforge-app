package com.testforge.user.mapper;

import com.testforge.user.dto.UserDto;
import com.testforge.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converts between the User entity (database shape) and UserDto (API shape).
 * Kept as its own class so the conversion logic is written exactly once;
 * every service that needs it just calls toDto().
 *
 * @Component registers it with Spring so it can be injected anywhere.
 */
@Component
public class UserMapper {

    /** Entity -> DTO. Note: password is intentionally never copied. */
    public UserDto toDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())   // enum -> its text name
                .createdAt(user.getCreatedAt())
                .build();
    }
}
