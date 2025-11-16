package com.daniil.bookingapp.mapper;

import com.daniil.bookingapp.dto.user.UpdateUserProfileRequestDto;
import com.daniil.bookingapp.dto.user.UserRegistrationRequestDto;
import com.daniil.bookingapp.dto.user.UserResponseDto;
import com.daniil.bookingapp.model.Role;
import com.daniil.bookingapp.model.User;
import com.daniil.bookingapp.model.enums.RoleName;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "password")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "payments", ignore = true)
    User toEntity(UserRegistrationRequestDto dto);

    @Mapping(target = "roles", expression = "java(extractRoleNames(user.getRoles()))")
    UserResponseDto toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "payments", ignore = true)
    void updateEntity(@MappingTarget User user, UpdateUserProfileRequestDto dto);

    default Set<RoleName> extractRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
