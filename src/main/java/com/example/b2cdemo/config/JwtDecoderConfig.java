package com.example.b2cdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;

/**
 * Defines the {@link JwtDecoder} bean separately from {@link SecurityConfig}.
 *
 * <p>Separation allows {@code @WebMvcTest} slices to replace this bean with a
 * {@code @MockBean JwtDecoder} without needing real Entra External ID properties.
 *
 * <p>Unlike old Azure AD B2C (which required a hardcoded JWKS URI including the policy name),
 * Microsoft Entra External ID supports standard OIDC discovery. At startup, Spring fetches:
 * {@code https://{tenant}.ciamlogin.com/{tenantId}/v2.0/.well-known/openid-configuration}
 * to auto-discover the JWKS endpoint and signing keys.
 *
 * <p>Validations applied to every incoming JWT:
 * <ol>
 *   <li><b>Issuer</b> — must match the Entra External ID tenant's issuer URI exactly</li>
 *   <li><b>Audience</b> — must contain this app's client ID (prevents cross-app token reuse)</li>
 *   <li><b>Expiry / not-before</b> — handled by Spring's default validators</li>
 * </ol>
 */
@Configuration
public class JwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder(AzureB2cProperties b2cProperties) {
        // JwtDecoders.fromIssuerLocation() fetches the OIDC discovery document at startup
        // to auto-discover the JWKS URI — no need to hardcode it.
        NimbusJwtDecoder decoder = (NimbusJwtDecoder)
                JwtDecoders.fromIssuerLocation(b2cProperties.issuerUri());

        // Audience validation is not included in the default validators — add it explicitly.
        // This prevents tokens issued for a different app (same tenant) from being accepted.
        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(
                        JwtClaimNames.AUD,
                        aud -> aud != null && aud.contains(b2cProperties.clientId())
                );

        // Replace decoder's validator with one that checks issuer + expiry + audience.
        // createDefaultWithIssuer() covers: timestamp (exp/nbf) + issuer (iss).
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(b2cProperties.issuerUri()),
                audienceValidator
        ));

        return decoder;
    }
}
