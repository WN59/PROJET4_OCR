package com.openclassrooms.starterjwt.security.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Couvre UserDetailsImpl : flags Spring Security + equals (branches).
 */
@DisplayName("UserDetailsImpl (unitaire)")
class UserDetailsImplTest {

    @Test
    @DisplayName("les flags de compte devraient tous être true et authorities vides")
    void shouldExposeDefaultSecurityFlags() {
        UserDetailsImpl user = UserDetailsImpl.builder()
                .id(1L)
                .username("yoga@studio.com")
                .firstName("Admin")
                .lastName("Admin")
                .admin(true)
                .password("pwd")
                .build();

        assertThat(user.getAuthorities()).isEmpty();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getUsername()).isEqualTo("yoga@studio.com");
        assertThat(user.getPassword()).isEqualTo("pwd");
        assertThat(user.getAdmin()).isTrue();
    }

    @Test
    @DisplayName("equals devrait comparer uniquement sur l'id")
    void shouldCompareById() {
        UserDetailsImpl a = UserDetailsImpl.builder().id(1L).username("a").password("p").build();
        UserDetailsImpl sameId = UserDetailsImpl.builder().id(1L).username("b").password("p").build();
        UserDetailsImpl otherId = UserDetailsImpl.builder().id(2L).username("a").password("p").build();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(sameId);
        assertThat(a).isNotEqualTo(otherId);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("pas-un-user");
    }
}
