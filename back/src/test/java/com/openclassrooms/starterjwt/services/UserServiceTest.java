package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.exception.UnauthorizedException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService (unitaire)")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    /** Jeu de données partagé. */
    private List<User> users;

    @BeforeEach
    void setUp() {
        users = List.of(
                User.builder()
                        .id(1L)
                        .email("yoga@studio.com")
                        .firstName("Admin")
                        .lastName("Admin")
                        .password("encoded")
                        .admin(true)
                        .build(),
                User.builder()
                        .id(2L)
                        .email("user@studio.com")
                        .firstName("Yoga")
                        .lastName("User")
                        .password("encoded")
                        .admin(false)
                        .build()
        );
    }

    // -------------------------------------------------------------------------
    // findById / getById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById devrait retourner l'utilisateur s'il existe")
    void shouldFindUserById() {
        User expected = users.get(0);
        when(userRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        Optional<User> result = userService.findById(expected.getId());

        assertThat(result).isPresent().contains(expected);
        verify(userRepository).findById(expected.getId());
    }

    @Test
    @DisplayName("findById devrait retourner Optional vide si l'utilisateur n'existe pas")
    void shouldReturnEmptyWhenUserNotFoundById() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(userService.findById(99L)).isEmpty();
        verify(userRepository).findById(99L);
    }

    @Test
    @DisplayName("getById devrait retourner l'utilisateur s'il existe")
    void shouldGetUserById() {
        User expected = users.get(0);
        when(userRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        User result = userService.getById(expected.getId());

        assertThat(result).isEqualTo(expected);
        verify(userRepository).findById(expected.getId());
    }

    @Test
    @DisplayName("getById devrait lever NotFoundException si l'utilisateur n'existe pas")
    void shouldThrowNotFoundWhenGetByIdMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // findByEmail / existsByEmail
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByEmail devrait retourner l'utilisateur correspondant")
    void shouldFindUserByEmail() {
        User expected = users.get(1);
        when(userRepository.findByEmail(expected.getEmail())).thenReturn(Optional.of(expected));

        Optional<User> result = userService.findByEmail(expected.getEmail());

        assertThat(result).isPresent().contains(expected);
        verify(userRepository).findByEmail(expected.getEmail());
    }

    @Test
    @DisplayName("existsByEmail devrait retourner true si l'email existe")
    void shouldReturnTrueWhenEmailExists() {
        when(userRepository.existsByEmail("yoga@studio.com")).thenReturn(true);

        assertThat(userService.existsByEmail("yoga@studio.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail devrait retourner false si l'email n'existe pas")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        when(userRepository.existsByEmail("new@studio.com")).thenReturn(false);

        assertThat(userService.existsByEmail("new@studio.com")).isFalse();
    }

    @Test
    @DisplayName("existsByEmail devrait retourner false si le repository renvoie null")
    void shouldReturnFalseWhenExistsByEmailIsNull() {
        when(userRepository.existsByEmail("new@studio.com")).thenReturn(null);

        assertThat(userService.existsByEmail("new@studio.com")).isFalse();
    }

    // -------------------------------------------------------------------------
    // create / register
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create devrait déléguer la sauvegarde au repository")
    void shouldCreateUser() {
        User toSave = users.get(1);
        when(userRepository.save(toSave)).thenReturn(toSave);

        User result = userService.create(toSave);

        assertThat(result).isEqualTo(toSave);
        verify(userRepository).save(toSave);
    }

    @Test
    @DisplayName("register devrait créer un utilisateur non-admin si l'email est libre")
    void shouldRegisterNewUser() {
        when(userRepository.existsByEmail("new@studio.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register("new@studio.com", "Studio", "Yoga", "encoded-pwd");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("new@studio.com");
        assertThat(saved.getLastName()).isEqualTo("Studio");
        assertThat(saved.getFirstName()).isEqualTo("Yoga");
        assertThat(saved.getPassword()).isEqualTo("encoded-pwd");
        assertThat(saved.isAdmin()).isFalse();
        assertThat(result).isEqualTo(saved);
    }

    @Test
    @DisplayName("register devrait lever BadRequestException si l'email est déjà pris")
    void shouldThrowBadRequestWhenEmailAlreadyTaken() {
        when(userRepository.existsByEmail("yoga@studio.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.register("yoga@studio.com", "Admin", "Admin", "encoded"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    // -------------------------------------------------------------------------
    // delete / deleteAccount
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("delete devrait appeler deleteById sur le repository")
    void shouldDeleteUserById() {
        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteAccount devrait supprimer le compte si l'email JWT correspond")
    void shouldDeleteOwnAccount() {
        User expected = users.get(1);
        when(userRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        userService.deleteAccount(expected.getId(), expected.getEmail());

        verify(userRepository).deleteById(expected.getId());
    }

    @Test
    @DisplayName("deleteAccount devrait lever UnauthorizedException si on cible un autre compte")
    void shouldThrowUnauthorizedWhenDeletingAnotherAccount() {
        User expected = users.get(1);
        when(userRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        assertThatThrownBy(() ->
                userService.deleteAccount(expected.getId(), "hacker@studio.com"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not allowed");

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteAccount devrait lever NotFoundException si le compte n'existe pas")
    void shouldThrowNotFoundWhenDeletingMissingAccount() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount(99L, "yoga@studio.com"))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
