package com.example.b2cdemo.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfileRequest(

        @Size(max = 255, message = "Display name must not exceed 255 characters")
        String displayName,

        @Pattern(
            regexp = "^[+]?[0-9\\s\\-().]{7,20}$",
            message = "Phone number format is invalid"
        )
        String phone
) {}
