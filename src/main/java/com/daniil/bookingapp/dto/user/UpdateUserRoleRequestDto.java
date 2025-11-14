package com.daniil.bookingapp.dto.user;

import com.daniil.bookingapp.model.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequestDto {

    @NotNull(message = "Role name cannot be null")
    private RoleName roleName;
}
