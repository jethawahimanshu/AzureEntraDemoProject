package com.example.b2cdemo.config;

import com.example.b2cdemo.support.JwtTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class JwtClaimsConverterTest {

    private final JwtClaimsConverter converter = new JwtClaimsConverter();

    @Test
    void shouldMapRolesClaimToRolePrefixedAuthorities() {
        var token = converter.convert(JwtTestFactory.adminJwt());

        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_Admin");
    }

    @Test
    void shouldMapScpClaimToScopePrefixedAuthorities() {
        var token = converter.convert(JwtTestFactory.writerJwt());

        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("SCOPE_products.read", "SCOPE_products.write");
    }

    @Test
    void shouldUseOidAsPrincipalName() {
        AbstractAuthenticationToken token = converter.convert(JwtTestFactory.userJwt());

        assertThat(token.getName()).isEqualTo(JwtTestFactory.TEST_OID);
    }

    @Test
    void shouldHandleEmptyRolesClaimGracefully() {
        // userJwt() has empty roles list — should not throw
        var token = converter.convert(JwtTestFactory.userJwt());

        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .noneMatch(a -> a.startsWith("ROLE_"));
    }

    @Test
    void shouldMergeRolesAndScopesIntoSingleAuthorityCollection() {
        var token = converter.convert(JwtTestFactory.adminJwt());

        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_Admin", "SCOPE_products.read", "SCOPE_products.write");
    }
}
