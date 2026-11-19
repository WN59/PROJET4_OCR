package com.openclassrooms.starterjwt.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Charge la config sécurité réelle (profil test) pour couvrir WebSecurityConfig.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("WebSecurityConfig (intégration Spring)")
class WebSecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DaoAuthenticationProvider authenticationProvider;

    @Test
    @DisplayName("les beans de sécurité devraient être créés")
    void shouldLoadSecurityBeans() {
        assertThat(securityFilterChain).isNotNull();
        assertThat(passwordEncoder).isNotNull();
        assertThat(authenticationManager).isNotNull();
        assertThat(authenticationProvider).isNotNull();
        assertThat(passwordEncoder.encode("test")).isNotBlank();
    }
}
