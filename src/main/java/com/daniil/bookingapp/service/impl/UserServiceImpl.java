package com.daniil.bookingapp.service.impl;

import com.daniil.bookingapp.dto.user.UpdateUserProfileRequestDto;
import com.daniil.bookingapp.dto.user.UpdateUserRoleRequestDto;
import com.daniil.bookingapp.dto.user.UserRegistrationRequestDto;
import com.daniil.bookingapp.dto.user.UserResponseDto;
import com.daniil.bookingapp.exception.EntityNotFoundException;
import com.daniil.bookingapp.exception.RegistrationException;
import com.daniil.bookingapp.mapper.UserMapper;
import com.daniil.bookingapp.model.Role;
import com.daniil.bookingapp.model.User;
import com.daniil.bookingapp.model.enums.RoleName;
import com.daniil.bookingapp.repository.RoleRepository;
import com.daniil.bookingapp.repository.UserRepository;
import com.daniil.bookingapp.service.UserService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Email already registered");
        }

        User user = userMapper.toEntity(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException("Role CUSTOMER not found"));

        user.getRoles().add(customerRole);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getProfile(User user) {
        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponseDto updateProfile(User user, UpdateUserProfileRequestDto requestDto) {
        userMapper.updateEntity(user, requestDto);
        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    @Transactional
    public UserResponseDto updateUserRole(Long userId, UpdateUserRoleRequestDto requestDto) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: "
                        + userId));

        Role role = roleRepository.findByName(requestDto.getRoleName())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: "
                        + requestDto.getRoleName()));

        user.getRoles().clear();
        user.getRoles().add(role);

        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    @Transactional
    public void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: "
                        + email));
    }
}
