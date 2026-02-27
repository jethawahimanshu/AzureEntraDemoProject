package com.example.b2cdemo.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

/**
 * Utility methods for extracting Azure AD B2C claims from a {@link Jwt}.
 *
 * <p>Centralises claim extraction so controllers and services don't embed
 * raw claim strings ("oid", "emails") throughout the codebase.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class — no instantiation
    }

    /**
     * Returns the immutable Azure B2C object ID ({@code oid} claim).
     *
     * <p>The {@code oid} claim is the stable, tenant-scoped user identifier.
     * Use this — not {@code sub} — as the primary key for user records.
     *
     * @throws IllegalStateException if the claim is absent (indicates a misconfigured B2C policy)
     */
    public static String extractOid(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        if (oid == null || oid.isBlank()) {
            throw new IllegalStateException(
                    "JWT is missing the required 'oid' claim. " +
                    "Verify that the B2C user flow includes 'User's Object ID' in the token claims.");
        }
        return oid;
    }

    /**
     * Returns the first email address from the {@code emails} array claim, if present.
     * B2C puts email addresses in an array (users may have multiple federated identities).
     */
    public static Optional<String> extractEmail(Jwt jwt) {
        List<String> emails = jwt.getClaimAsStringList("emails");
        if (emails != null && !emails.isEmpty()) {
            return Optional.of(emails.get(0));
        }
        return Optional.empty();
    }

    /**
     * Returns the display name from the {@code name} claim, if present.
     */
    public static Optional<String> extractDisplayName(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString("name"));
    }
}
