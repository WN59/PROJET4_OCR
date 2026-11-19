package com.openclassrooms.starterjwt.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Classe quasi vide (implémentation commentée dans le starter) — on couvre au moins l'instanciation.
 */
@DisplayName("AuthEntryPointJwt (unitaire)")
class AuthEntryPointJwtTest {

    @Test
    @DisplayName("devrait pouvoir être instancié")
    void shouldInstantiate() {
        assertThat(new AuthEntryPointJwt()).isNotNull();
    }
}
