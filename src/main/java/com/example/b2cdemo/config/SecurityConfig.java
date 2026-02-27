package com.example.b2cdemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central Spring Security configuration.
 *
 * <p>The {@link JwtDecoder} is defined in {@link JwtDecoderConfig} and injected here.
 * This separation allows {@code @WebMvcTest} slices to replace the decoder with a
 * {@code @MockBean JwtDecoder} without needing real Azure B2C property values.
 *
 * <p>Security design decisions:
 * <ul>
 *   <li>STATELESS — no HTTP sessions; every request must carry a Bearer token</li>
 *   <li>CSRF disabled — safe for stateless REST APIs (no browser cookie-based auth)</li>
 *   <li>Defense in depth — URL-level rules AND {@code @PreAuthorize} on controllers</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtClaimsConverter jwtClaimsConverter;
    private final JwtDecoder jwtDecoder;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Public — no token required
                .requestMatchers("/api/public/**").permitAll()

                // Actuator: health & info are public; everything else requires Admin
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("Admin")

                // Swagger UI — open without a token (restrict via profile in production)
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Admin — requires ROLE_Admin (from Azure App Roles in the JWT)
                .requestMatchers("/api/admin/**").hasRole("Admin")

                // Products — scope-based, read vs write separated by HTTP method
                .requestMatchers(HttpMethod.GET,    "/api/products/**").hasAuthority("SCOPE_products.read")
                .requestMatchers(HttpMethod.POST,   "/api/products/**").hasAuthority("SCOPE_products.write")
                .requestMatchers(HttpMethod.PUT,    "/api/products/**").hasAuthority("SCOPE_products.write")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("SCOPE_products.write")

                // User profile — any authenticated principal
                .requestMatchers("/api/user/**").authenticated()

                // Deny everything not explicitly listed above
                .anyRequest().denyAll()
            )

            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtClaimsConverter)
                )
                // Return RFC 6750 WWW-Authenticate headers on 401 and 403
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )

            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
