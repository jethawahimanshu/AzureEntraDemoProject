package com.example.b2cdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA auditing configuration — kept in a separate class so that
 * {@code @WebMvcTest} slices do not pick it up and fail trying to resolve
 * {@code jpaMappingContext} in a context that has no JPA layer.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
