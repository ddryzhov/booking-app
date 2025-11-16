package com.daniil.bookingapp.service.impl;

import com.daniil.bookingapp.dto.user.UserLoginRequestDto;
import com.daniil.bookingapp.dto.user.UserLoginResponseDto;
import com.daniil.bookingapp.dto.user.UserRegistrationRequestDto;
import com.daniil.bookingapp.dto.user.UserResponseDto;
import com.daniil.bookingapp.exception.AuthenticationException;
import com.daniil.bookingapp.model.User;
import com.daniil.bookingapp.security.jwt.JwtUtil;
import com.daniil.bookingapp.service.AuthenticationService;
import com.daniil.bookingapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        return userService.register(requestDto);
    }

    @Override
    public UserLoginResponseDto login(UserLoginRequestDto requestDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(),
                            requestDto.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid email or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(requestDto.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        userService.updateLastLogin((User) userDetails);

        UserResponseDto userDto = userService.getProfile((User) userDetails);

        return UserLoginResponseDto.builder()
                .token(token)
                .user(userDto)
                .build();
    }
}
