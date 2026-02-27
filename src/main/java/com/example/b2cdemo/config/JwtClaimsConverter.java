package com.example.b2cdemo.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Converts Azure AD B2C JWT claims into Spring Security {@link GrantedAuthority} objects.
 *
 * <p>Authority sources:
 * <ul>
 *   <li>{@code scp} claim  — space-delimited OAuth2 scopes → {@code SCOPE_{scope}}</li>
 *   <li>{@code roles} claim — array of Azure App Role names → {@code ROLE_{roleName}}</li>
 * </ul>
 *
 * <p>The principal name is set to the {@code oid} claim (immutable Azure object ID),
 * which survives email changes and account merges.
 */
@Component
public class JwtClaimsConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    // Delegates scp → SCOPE_ mapping to Spring's built-in converter
    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);

        List<GrantedAuthority> roleAuthorities = extractRoleAuthorities(jwt);

        Collection<GrantedAuthority> merged = Stream.concat(
                scopeAuthorities != null ? scopeAuthorities.stream() : Stream.empty(),
                roleAuthorities.stream()
        ).toList();

        // Use "oid" as principal name — it is the stable, immutable user identifier in B2C
        String principalName = jwt.getClaimAsString("oid");

        return new JwtAuthenticationToken(jwt, merged, principalName);
    }

    private List<GrantedAuthority> extractRoleAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
