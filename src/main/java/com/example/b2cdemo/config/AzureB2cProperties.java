package com.example.b2cdemo.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed configuration properties for Microsoft Entra External ID
 * (the Azure AD B2C replacement for new customers as of May 2025).
 *
 * <p>Required env vars:
 * <pre>
 *   ENTRA_TENANT_NAME   — subdomain, e.g. "myapp" (part before .ciamlogin.com)
 *   ENTRA_TENANT_ID     — directory GUID: Entra Portal → Overview → Tenant ID
 *   ENTRA_CLIENT_ID     — App Registration → Application (client) ID
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "azure.b2c")
public record AzureB2cProperties(
        @NotBlank String tenantName,
        @NotBlank String tenantId,
        @NotBlank String clientId
) {
    /**
     * Entra External ID issuer URI — matches the {@code iss} claim in issued JWTs.
     * Spring uses this as the base for OIDC discovery:
     * {@code {issuerUri}/.well-known/openid-configuration}
     */
    public String issuerUri() {
        return "https://%s.ciamlogin.com/%s/v2.0".formatted(tenantName, tenantId);
    }
}
