package com.example.b2cdemo.controller;

import com.example.b2cdemo.dto.response.UserProfileResponse;
import com.example.b2cdemo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoints. Requires the {@code Admin} Azure App Role.
 *
 * <p>Security is enforced at two levels (defense in depth):
 * <ol>
 *   <li>URL-level — {@code /api/admin/**} requires {@code ROLE_Admin} in {@code SecurityConfig}</li>
 *   <li>Method-level — {@code @PreAuthorize} on the class catches any future refactoring gaps</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only operations — requires the 'Admin' Azure App Role")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('Admin')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<Page<UserProfileResponse>> listUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(userService.findAllUsers(pageable));
    }

    @GetMapping("/users/{oid}")
    @Operation(summary = "Get any user by their Azure B2C object ID")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable String oid) {
        return ResponseEntity.ok(userService.getProfileByOid(oid));
    }

    @DeleteMapping("/users/{oid}")
    @Operation(summary = "Delete any user's local profile by their Azure B2C object ID")
    public ResponseEntity<Void> deleteUser(@PathVariable String oid) {
        userService.deleteProfile(oid);
        return ResponseEntity.noContent().build();
    }
}
