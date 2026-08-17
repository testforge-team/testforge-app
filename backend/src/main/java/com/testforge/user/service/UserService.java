package com.testforge.user.service;

import com.testforge.exception.ResourceNotFoundException;
import com.testforge.user.dto.UserDto;
import com.testforge.user.entity.User;
import com.testforge.user.mapper.UserMapper;
import com.testforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for viewing/managing users (the admin's "Manage Users" area).
 *
 * @RequiredArgsConstructor (Lombok) generates a constructor for the two final
 * fields below; Spring sees that constructor and automatically injects the
 * repository and mapper. This is "dependency injection" - we never write
 * 'new UserRepository()' ourselves; Spring hands us ready-made objects.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * All users, converted to safe DTOs.
     * findAll() comes free from JpaRepository; .stream().map() converts
     * each entity to a DTO one by one; .toList() collects the results.
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * One user by id, or a clean 404 if the id doesn't exist.
     * orElseThrow(): findById returns an Optional (a box that may be empty);
     * if the box is empty we throw, and GlobalExceptionHandler turns that
     * into an HTTP 404 with our message.
     */
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }
}
