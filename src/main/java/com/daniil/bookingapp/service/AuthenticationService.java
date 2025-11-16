package com.daniil.bookingapp.service;

import com.daniil.bookingapp.dto.user.UserLoginRequestDto;
import com.daniil.bookingapp.dto.user.UserLoginResponseDto;
import com.daniil.bookingapp.dto.user.UserRegistrationRequestDto;
import com.daniil.bookingapp.dto.user.UserResponseDto;

public interface AuthenticationService {
    UserResponseDto register(UserRegistrationRequestDto requestDto);

    UserLoginResponseDto login(UserLoginRequestDto requestDto);
}
