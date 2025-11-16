package com.daniil.bookingapp.service;

import com.daniil.bookingapp.dto.user.UpdateUserProfileRequestDto;
import com.daniil.bookingapp.dto.user.UpdateUserRoleRequestDto;
import com.daniil.bookingapp.dto.user.UserRegistrationRequestDto;
import com.daniil.bookingapp.dto.user.UserResponseDto;
import com.daniil.bookingapp.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto);

    UserResponseDto getProfile(User user);

    UserResponseDto updateProfile(User user, UpdateUserProfileRequestDto requestDto);

    UserResponseDto updateUserRole(Long userId, UpdateUserRoleRequestDto requestDto);

    void updateLastLogin(User user);

    User findByEmail(String email);
}
