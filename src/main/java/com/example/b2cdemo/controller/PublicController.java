package com.example.b2cdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Publicly accessible endpoints — no Bearer token required.
 */
@RestController
@RequestMapping("/api/public")
@Tag(name = "Public", description = "Unauthenticated endpoints")
public class PublicController {

    @GetMapping("/health")
    @Operation(summary = "Application health check")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/info")
    @Operation(summary = "Application information")
    public Map<String, String> info() {
        return Map.of(
                "name", "Azure B2C Demo API",
                "version", "1.0.0",
                "description", "Spring Boot 3.x secured with Azure AD B2C"
        );
    }
}
