package com.example.b2cdemo.controller;

import com.example.b2cdemo.dto.request.UserProfileRequest;
import com.example.b2cdemo.dto.response.UserProfileResponse;
import com.example.b2cdemo.service.UserService;
import com.example.b2cdemo.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated user profile endpoints.
 * Requires a valid Azure B2C Bearer token. Any authenticated user may access their own profile.
 *
 * <p>{@code @AuthenticationPrincipal Jwt jwt} is the idiomatic Spring Security way to
 * inject the decoded JWT into a controller method without casting from the SecurityContext.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Manage the authenticated user's profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user's profile (auto-provisioned from token on first call)")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String oid = SecurityUtils.extractOid(jwt);
        return ResponseEntity.ok(userService.getProfile(oid, jwt));
    }

    @PutMapping("/profile")
    @Operation(summary = "Create or update current user's profile")
    public ResponseEntity<UserProfileResponse> upsertProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserProfileRequest request) {
        String oid = SecurityUtils.extractOid(jwt);
        return ResponseEntity.ok(userService.upsertProfile(oid, jwt, request));
    }

    @DeleteMapping("/profile")
    @Operation(summary = "Delete current user's local profile data")
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal Jwt jwt) {
        String oid = SecurityUtils.extractOid(jwt);
        userService.deleteProfile(oid);
        return ResponseEntity.noContent().build();
    }
}
