package com.openclassrooms.starterjwt.services;

/**
 * =============================================================================
 * FICHIER DE RÉFÉRENCE — Test unitaire JUnit 5 + Mockito (TeacherService)
 * =============================================================================
 *
 * Type de test : UNITAIRE (back)
 * - On teste UNIQUEMENT TeacherService.
 * - TeacherRepository est un MOCK → pas de vraie BDD.
 * - Pas de @SpringBootTest : plus rapide, plus isolé.
 *
 * Méthodes couvertes : findAll / findById / getById (succès + absences).
 * =============================================================================
 */

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService (unitaire)")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    /** Jeu de données partagé : un seul “tableau” de teachers pour tous les tests. */
    private List<Teacher> teachers;

    @BeforeEach
    void setUp() {
        teachers = List.of(
                Teacher.builder().id(1L).firstName("Margot").lastName("DELAHAYE").build(),
                Teacher.builder().id(2L).firstName("Hélène").lastName("THIERCELIN").build()
        );
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll devrait retourner la liste des teachers du repository")
    void shouldReturnAllTeachers() {
        when(teacherRepository.findAll()).thenReturn(teachers);

        List<Teacher> result = teacherService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactlyElementsOf(teachers)
                .extracting(Teacher::getFirstName)
                .containsExactly("Margot", "Hélène");
        verify(teacherRepository).findAll();
    }

    @Test
    @DisplayName("findAll devrait retourner une liste vide si aucun teacher")
    void shouldReturnEmptyListWhenNoTeachers() {
        when(teacherRepository.findAll()).thenReturn(Collections.emptyList());

        List<Teacher> result = teacherService.findAll();

        assertThat(result).isEmpty();
        verify(teacherRepository).findAll();
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById devrait retourner un Optional avec le teacher s'il existe")
    void shouldReturnTeacherWhenFoundById() {
        Teacher expected = teachers.get(0); // Margot
        when(teacherRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        Optional<Teacher> result = teacherService.findById(expected.getId());

        assertThat(result).isPresent().contains(expected);
        verify(teacherRepository).findById(expected.getId());
    }

    @Test
    @DisplayName("findById devrait retourner un Optional vide si le teacher n'existe pas")
    void shouldReturnEmptyOptionalWhenTeacherNotFound() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Teacher> result = teacherService.findById(99L);

        assertThat(result).isEmpty();
        verify(teacherRepository).findById(99L);
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getById devrait retourner le teacher s'il existe")
    void shouldReturnTeacherWhenGetByIdExists() {
        Teacher expected = teachers.get(0); // Margot
        when(teacherRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        Teacher result = teacherService.getById(expected.getId());

        assertThat(result).isEqualTo(expected);
        assertThat(result.getFirstName()).isEqualTo("Margot");
        verify(teacherRepository).findById(expected.getId());
    }

    @Test
    @DisplayName("getById devrait lever NotFoundException si le teacher n'existe pas")
    void shouldThrowNotFoundWhenGetByIdMissing() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(teacherRepository).findById(99L);
    }
}
