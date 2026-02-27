package com.example.b2cdemo.dto.response;

import com.example.b2cdemo.domain.enums.ProductCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        ProductCategory category,
        Integer stockQuantity,
        Instant createdAt,
        Instant updatedAt
) {}
