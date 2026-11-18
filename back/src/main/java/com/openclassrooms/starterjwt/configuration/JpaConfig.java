package com.openclassrooms.starterjwt.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Active l'audit JPA ({@code createdAt} / {@code updatedAt}).
 * <p>
 * Volontairement séparé de {@code @SpringBootApplication} : ainsi les tests
 * {@code @WebMvcTest} ne chargent pas JPA (évite "JPA metamodel must not be empty").
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
