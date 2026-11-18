package com.openclassrooms.starterjwt.services;

/**
 * =============================================================================
 * FICHIER DE RÉFÉRENCE — Service le plus complexe (SessionService)
 * =============================================================================
 *
 * Type : UNITAIRE (JUnit 5 + Mockito)
 *
 * Pourquoi “le plus complexe” ?
 * - 2 dépendances mockées : SessionRepository + UserService
 * - CRUD classique (create/find/update/delete)
 * - + règles métier participate / noLongerParticipate (branches if / exceptions)
 *
 * Bonnes pratiques respectées (Spring Boot 3.5 / Java 21 / 2026) :
 * - JUnit 5 (@Test, @BeforeEach, @DisplayName) — plus de JUnit 4
 * - @ExtendWith(MockitoExtension.class) — mocks sans démarrer Spring
 * - AssertJ (assertThat / assertThatThrownBy) — assertions fluides
 * - Un @Test = un cas du plan (succès OU erreur, pas les deux)
 * - verify(...) pour prouver les appels (ou never() si on ne doit PAS sauver)
 * - Listes mutables (ArrayList) quand le service modifie session.getUsers()
 *
 * Équivalent front :
 * - @Mock ≈ jest.fn()
 * - when(...).thenReturn(...) ≈ mockReturnValue
 * - verify(...) ≈ toHaveBeenCalled
 * =============================================================================
 */

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService (unitaire)")
class SessionServiceTest {

    /** Accès BDD fake : on contrôle ce que findById / save renvoient. */
    @Mock
    private SessionRepository sessionRepository;

    /**
     * Autre service fake.
     * SessionService.participate() appelle userService.getById(...) :
     * on mocke aussi cette dépendance (pas de vraie chaîne UserRepository).
     */
    @Mock
    private UserService userService;

    /** Vraie classe sous test : Mockito injecte les 2 @Mock dans le constructeur. */
    @InjectMocks
    private SessionService sessionService;

    /** Jeux de données partagés (comme un “tableau fixtures”). */
    private List<Session> sessions;
    private List<User> users;
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacher = Teacher.builder()
                .id(1L)
                .firstName("Margot")
                .lastName("DELAHAYE")
                .build();

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

        // ArrayList (mutable) : participate() fait session.getUsers().add(...)
        sessions = List.of(
                Session.builder()
                        .id(1L)
                        .name("Yoga débutant")
                        .description("Séance pour débutants")
                        .date(new Date())
                        .teacher(teacher)
                        .users(new ArrayList<>())
                        .build(),
                Session.builder()
                        .id(2L)
                        .name("Yoga avancé")
                        .description("Séance avancée")
                        .date(new Date())
                        .teacher(teacher)
                        .users(new ArrayList<>(List.of(users.get(1)))) // déjà inscrit
                        .build()
        );
    }

    // -------------------------------------------------------------------------
    // create / findAll / findById / getById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create devrait sauvegarder la session")
    void shouldCreateSession() {
        Session toCreate = sessions.get(0);
        when(sessionRepository.save(toCreate)).thenReturn(toCreate);

        Session result = sessionService.create(toCreate);

        assertThat(result).isEqualTo(toCreate);
        verify(sessionRepository).save(toCreate);
    }

    @Test
    @DisplayName("findAll devrait retourner toutes les sessions")
    void shouldReturnAllSessions() {
        when(sessionRepository.findAll()).thenReturn(sessions);

        List<Session> result = sessionService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactlyElementsOf(sessions)
                .extracting(Session::getName)
                .containsExactly("Yoga débutant", "Yoga avancé");
        verify(sessionRepository).findAll();
    }

    @Test
    @DisplayName("findAll devrait retourner une liste vide si aucune session")
    void shouldReturnEmptyListWhenNoSessions() {
        when(sessionRepository.findAll()).thenReturn(Collections.emptyList());

        assertThat(sessionService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findById devrait retourner la session si elle existe")
    void shouldFindSessionById() {
        Session expected = sessions.get(0);
        when(sessionRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        assertThat(sessionService.findById(expected.getId())).isPresent().contains(expected);
    }

    @Test
    @DisplayName("findById devrait retourner Optional vide si la session n'existe pas")
    void shouldReturnEmptyWhenSessionNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(sessionService.findById(99L)).isEmpty();
    }

    @Test
    @DisplayName("getById devrait retourner la session si elle existe")
    void shouldGetSessionById() {
        Session expected = sessions.get(0);
        when(sessionRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        assertThat(sessionService.getById(expected.getId())).isEqualTo(expected);
    }

    @Test
    @DisplayName("getById devrait lever NotFoundException si la session n'existe pas")
    void shouldThrowNotFoundWhenGetByIdMissing() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // update / delete
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update devrait forcer l'id et sauvegarder si la session existe")
    void shouldUpdateSession() {
        Session existing = sessions.get(0);
        // payload sans id : le service doit faire session.setId(id)
        Session payload = Session.builder()
                .name("Yoga modifié")
                .description("Nouvelle description")
                .date(new Date())
                .teacher(teacher)
                .users(new ArrayList<>())
                .build();

        when(sessionRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(sessionRepository.save(payload)).thenReturn(payload);

        Session result = sessionService.update(existing.getId(), payload);

        assertThat(payload.getId()).isEqualTo(existing.getId());
        assertThat(result.getName()).isEqualTo("Yoga modifié");
        verify(sessionRepository).save(payload);
    }

    @Test
    @DisplayName("update devrait lever NotFoundException si la session n'existe pas")
    void shouldThrowNotFoundWhenUpdateMissingSession() {
        Session payload = sessions.get(0);
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.update(99L, payload))
                .isInstanceOf(NotFoundException.class);

        // Important : en cas d'erreur, on ne doit PAS écrire en base
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete devrait supprimer la session si elle existe")
    void shouldDeleteSession() {
        Session existing = sessions.get(0);
        when(sessionRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        sessionService.delete(existing.getId());

        verify(sessionRepository).deleteById(existing.getId());
    }

    @Test
    @DisplayName("delete devrait lever NotFoundException si la session n'existe pas")
    void shouldThrowNotFoundWhenDeleteMissingSession() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.delete(99L))
                .isInstanceOf(NotFoundException.class);

        verify(sessionRepository, never()).deleteById(any());
    }

    // -------------------------------------------------------------------------
    // participate / noLongerParticipate  ← coeur métier (le plus important à comprendre)
    // -------------------------------------------------------------------------

    /**
     * Cas nominal participate :
     * 1) charger la session
     * 2) charger l'utilisateur via UserService
     * 3) vérifier qu'il n'est pas déjà inscrit
     * 4) l'ajouter à session.users puis save
     */
    @Test
    @DisplayName("participate devrait ajouter l'utilisateur à la session")
    void shouldParticipate() {
        Session session = sessions.get(0); // aucun participant
        User user = users.get(1);

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(userService.getById(user.getId())).thenReturn(user);
        when(sessionRepository.save(session)).thenReturn(session);

        sessionService.participate(session.getId(), user.getId());

        assertThat(session.getUsers()).containsExactly(user);
        verify(userService).getById(user.getId());
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("participate devrait lever BadRequestException si déjà inscrit")
    void shouldThrowBadRequestWhenAlreadyParticipating() {
        Session session = sessions.get(1); // user 2 déjà dans users
        User user = users.get(1);

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(userService.getById(user.getId())).thenReturn(user);

        assertThatThrownBy(() -> sessionService.participate(session.getId(), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already participates");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("participate devrait lever NotFoundException si la session n'existe pas")
    void shouldThrowNotFoundWhenParticipateMissingSession() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.participate(99L, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    /**
     * Cas nominal unparticipate :
     * filtre la liste des users pour retirer userId, puis save.
     */
    @Test
    @DisplayName("noLongerParticipate devrait retirer l'utilisateur de la session")
    void shouldNoLongerParticipate() {
        Session session = sessions.get(1); // user 2 inscrit
        Long userId = users.get(1).getId();

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(sessionRepository.save(session)).thenReturn(session);

        sessionService.noLongerParticipate(session.getId(), userId);

        assertThat(session.getUsers()).isEmpty();
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("noLongerParticipate devrait lever BadRequestException si non inscrit")
    void shouldThrowBadRequestWhenNotParticipating() {
        Session session = sessions.get(0); // liste vide

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.noLongerParticipate(session.getId(), 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not participate");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("noLongerParticipate devrait lever NotFoundException si la session n'existe pas")
    void shouldThrowNotFoundWhenUnparticipateMissingSession() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.noLongerParticipate(99L, 1L))
                .isInstanceOf(NotFoundException.class);
    }
}
