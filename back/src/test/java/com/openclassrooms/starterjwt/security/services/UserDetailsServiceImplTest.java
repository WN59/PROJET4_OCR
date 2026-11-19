package com.openclassrooms.starterjwt.security.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Couvre le bridge Spring Security ↔ UserRepository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl (unitaire)")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    @DisplayName("loadUserByUsername devrait renvoyer un UserDetailsImpl")
    void shouldLoadUserByEmail() {
        User user = User.builder()
                .id(1L)
                .email("yoga@studio.com")
                .firstName("Admin")
                .lastName("Admin")
                .password("hash")
                .admin(true)
                .build();
        when(userRepository.findByEmail("yoga@studio.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("yoga@studio.com");

        assertThat(details).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl impl = (UserDetailsImpl) details;
        assertThat(impl.getId()).isEqualTo(1L);
        assertThat(impl.getUsername()).isEqualTo("yoga@studio.com");
        assertThat(impl.getFirstName()).isEqualTo("Admin");
        assertThat(impl.getLastName()).isEqualTo("Admin");
        assertThat(impl.getAdmin()).isTrue();
        assertThat(impl.getPassword()).isEqualTo("hash");
        verify(userRepository).findByEmail("yoga@studio.com");
    }

    @Test
    @DisplayName("loadUserByUsername devrait lever UsernameNotFoundException si absent")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@test.com");
    }
}
