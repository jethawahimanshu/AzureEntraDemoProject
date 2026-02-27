package com.example.b2cdemo.dto.response;

import java.time.Instant;

public record UserProfileResponse(
        String oid,
        String displayName,
        String email,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {}
