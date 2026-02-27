package com.example.b2cdemo.support;

import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

/**
 * Factory for building realistic Azure AD B2C {@link Jwt} objects in tests.
 *
 * <p>Use these rather than constructing Jwt objects directly in each test,
 * so that B2C claim structure is defined in one place.
 */
public final class JwtTestFactory {

    public static final String TEST_OID   = "aaaabbbb-1111-2222-3333-ccccddddeeee";
    public static final String TEST_EMAIL = "testuser@example.com";
    public static final String TEST_NAME  = "Test User";
    public static final String ISSUER     = "https://mytenant.b2clogin.com/tenant-guid/v2.0/";
    public static final String CLIENT_ID  = "test-client-id";

    private JwtTestFactory() {}

    /** A token with {@code products.read} scope and no roles — a regular API consumer. */
    public static Jwt readerJwt() {
        return build(List.of("products.read"), List.of());
    }

    /** A token with both product scopes but no admin role. */
    public static Jwt writerJwt() {
        return build(List.of("products.read", "products.write"), List.of());
    }

    /** A token with the {@code Admin} app role and all product scopes. */
    public static Jwt adminJwt() {
        return build(List.of("products.read", "products.write"), List.of("Admin"));
    }

    /** A valid authenticated token with no scopes (just a logged-in user). */
    public static Jwt userJwt() {
        return build(List.of(), List.of());
    }

    private static Jwt build(List<String> scopes, List<String> roles) {
        String scp = String.join(" ", scopes);
        return Jwt.withTokenValue("mock-token-value")
                .header("alg", "RS256")
                .header("typ", "JWT")
                .claim("oid", TEST_OID)
                .claim("name", TEST_NAME)
                .claim("emails", List.of(TEST_EMAIL))
                .claim("scp", scp)
                .claim("roles", roles)
                .claim("iss", ISSUER)
                .claim("aud", List.of(CLIENT_ID))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
