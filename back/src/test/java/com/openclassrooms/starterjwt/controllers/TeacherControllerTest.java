package com.openclassrooms.starterjwt.controllers;

/**
 * =============================================================================
 * FICHIER DE RÉFÉRENCE — Test d'intégration controller (TeacherController)
 * =============================================================================
 *
 * Type : INTÉGRATION (couche web) — exigence OCR “Spring Boot Test” / API
 *
 * Différence avec SessionServiceTest (unitaire) :
 * - Ici on démarre une “tranche” Spring MVC (@WebMvcTest)
 * - On passe par de VRAIES URLs HTTP simulées (MockMvc)
 * - On vérifie status HTTP + JSON renvoyé
 * - Le métier TeacherService est TOUJOURS mocké (@MockitoBean)
 *
 * Stack 2026 (Spring Boot 3.5) :
 * - @WebMvcTest(TeacherController.class) → charge seulement la couche web
 * - @MockitoBean → remplace @MockBean (déprécié depuis Boot 3.4)
 * - MockMvc + jsonPath → assertions HTTP/JSON
 * - @AutoConfigureMockMvc(addFilters = false) → on teste le controller
 *   sans la chaîne JWT (la sécurité sera couverte à part / sur Auth)
 * - GlobalExceptionHandler (@RestControllerAdvice) est pris en charge
 *   → 404 / 400 testables comme en prod
 *
 * Équivalent mental Cypress :
 * - mockMvc.perform(get(...)) ≈ cy.request / visite d'endpoint
 * - status().isOk() ≈ cy.wait + expect status
 * - jsonPath("$.firstName") ≈ cy.contains / body assertion
 * =============================================================================
 */

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.mapper.TeacherMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TeacherController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TeacherController (intégration web)")
class TeacherControllerTest {

    /**
     * Client HTTP de test fourni par Spring.
     * perform(get/post/...) simule un appel navigateur/Postman.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * @MockitoBean (Boot 3.4+) : mock enregistré DANS le contexte Spring.
     * TeacherController recevra ce faux TeacherService.
     */
    @MockitoBean
    private TeacherService teacherService;

    /**
     * MapStruct n'est pas chargé par @WebMvcTest (ce n'est pas un @Controller).
     * On mocke aussi le mapper pour contrôler le DTO renvoyé.
     */
    @MockitoBean
    private TeacherMapper teacherMapper;

    private List<Teacher> teachers;
    private List<TeacherDto> teacherDtos;

    @BeforeEach
    void setUp() {
        teachers = List.of(
                Teacher.builder().id(1L).firstName("Margot").lastName("DELAHAYE").build(),
                Teacher.builder().id(2L).firstName("Hélène").lastName("THIERCELIN").build()
        );

        teacherDtos = List.of(
                new TeacherDto(1L, "DELAHAYE", "Margot", null, null),
                new TeacherDto(2L, "THIERCELIN", "Hélène", null, null)
        );
    }

    // -------------------------------------------------------------------------
    // GET /api/teacher
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/teacher devrait retourner 200 et la liste JSON des teachers")
    void shouldReturnAllTeachers() throws Exception {
        // Arrange : le controller appelle service puis mapper
        when(teacherService.findAll()).thenReturn(teachers);
        when(teacherMapper.toDto(teachers)).thenReturn(teacherDtos);

        // Act + Assert HTTP
        mockMvc.perform(get("/api/teacher").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Margot"))
                .andExpect(jsonPath("$[1].firstName").value("Hélène"));

        verify(teacherService).findAll();
        verify(teacherMapper).toDto(teachers);
    }

    // -------------------------------------------------------------------------
    // GET /api/teacher/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/teacher/{id} devrait retourner 200 et le teacher demandé")
    void shouldReturnTeacherById() throws Exception {
        Teacher teacher = teachers.get(0);
        TeacherDto dto = teacherDtos.get(0);

        when(teacherService.getById(1L)).thenReturn(teacher);
        when(teacherMapper.toDto(teacher)).thenReturn(dto);

        mockMvc.perform(get("/api/teacher/{id}", 1).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Margot"))
                .andExpect(jsonPath("$.lastName").value("DELAHAYE"));

        verify(teacherService).getById(1L);
    }

    /**
     * NotFoundException remontée par le service → GlobalExceptionHandler → HTTP 404.
     * C'est typiquement ce qu'on veut prouver en intégration web.
     */
    @Test
    @DisplayName("GET /api/teacher/{id} devrait retourner 404 si le teacher n'existe pas")
    void shouldReturn404WhenTeacherNotFound() throws Exception {
        when(teacherService.getById(99L))
                .thenThrow(new NotFoundException("Teacher not found with id: 99"));

        mockMvc.perform(get("/api/teacher/{id}", 99).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(teacherService).getById(99L);
    }

    /**
     * Long.valueOf("abc") lève NumberFormatException → handler → HTTP 400.
     */
    @Test
    @DisplayName("GET /api/teacher/{id} devrait retourner 400 si l'id n'est pas numérique")
    void shouldReturn400WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/teacher/{id}", "abc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
