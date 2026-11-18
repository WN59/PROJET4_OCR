package com.openclassrooms.starterjwt.controllers;

/**
 * Plan OCR — Sessions / Detail / Create / Update / Delete (API back).
 * Note : visibilité boutons Admin = surtout front ; ici on couvre les endpoints CRUD.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SessionController (intégration web)")
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private SessionMapper sessionMapper;

    private Session session;
    private SessionDto sessionDto;
    private List<Session> sessions;
    private List<SessionDto> sessionDtos;

    @BeforeEach
    void setUp() {
        Teacher teacher = Teacher.builder().id(1L).firstName("Margot").lastName("DELAHAYE").build();

        session = Session.builder()
                .id(1L)
                .name("Yoga débutant")
                .description("Séance pour débutants")
                .date(new Date())
                .teacher(teacher)
                .users(new ArrayList<>())
                .build();

        sessionDto = new SessionDto(
                1L,
                "Yoga débutant",
                session.getDate(),
                1L,
                "Séance pour débutants",
                List.of(),
                null,
                null
        );

        sessions = List.of(session);
        sessionDtos = List.of(sessionDto);
    }

    // -------------------------------------------------------------------------
    // Liste + détail (plan : Sessions / Informations session)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/session devrait retourner la liste des sessions")
    void shouldReturnAllSessions() throws Exception {
        when(sessionService.findAll()).thenReturn(sessions);
        when(sessionMapper.toDto(sessions)).thenReturn(sessionDtos);

        mockMvc.perform(get("/api/session").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Yoga débutant"));
    }

    @Test
    @DisplayName("GET /api/session/{id} devrait retourner le détail de la session")
    void shouldReturnSessionDetail() throws Exception {
        when(sessionService.getById(1L)).thenReturn(session);
        when(sessionMapper.toDto(session)).thenReturn(sessionDto);

        mockMvc.perform(get("/api/session/{id}", 1).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Yoga débutant"))
                .andExpect(jsonPath("$.description").value("Séance pour débutants"));
    }

    @Test
    @DisplayName("GET /api/session/{id} devrait retourner 404 si session introuvable")
    void shouldReturn404WhenSessionNotFound() throws Exception {
        when(sessionService.getById(99L)).thenThrow(new NotFoundException("Session not found"));

        mockMvc.perform(get("/api/session/{id}", 99))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // Création
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/session devrait créer une session")
    void shouldCreateSession() throws Exception {
        SessionDto payload = new SessionDto(
                null,
                "Yoga avancé",
                new Date(),
                1L,
                "Séance avancée",
                null,
                null,
                null
        );

        when(sessionMapper.toEntity(any(SessionDto.class))).thenReturn(session);
        when(sessionService.create(session)).thenReturn(session);
        when(sessionMapper.toDto(session)).thenReturn(sessionDto);

        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga débutant"));

        verify(sessionService).create(session);
    }

    @Test
    @DisplayName("POST /api/session devrait retourner 400 si champs requis manquants")
    void shouldReturn400WhenCreateFieldsMissing() throws Exception {
        SessionDto invalid = new SessionDto(); // name/date/teacher_id/description null

        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(sessionService, never()).create(any());
    }

    // -------------------------------------------------------------------------
    // Modification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/session/{id} devrait modifier une session")
    void shouldUpdateSession() throws Exception {
        SessionDto payload = new SessionDto(
                1L,
                "Yoga modifié",
                new Date(),
                1L,
                "Description modifiée",
                List.of(),
                null,
                null
        );
        SessionDto updatedDto = new SessionDto(
                1L,
                "Yoga modifié",
                payload.getDate(),
                1L,
                "Description modifiée",
                List.of(),
                null,
                null
        );

        when(sessionMapper.toEntity(any(SessionDto.class))).thenReturn(session);
        when(sessionService.update(eq(1L), eq(session))).thenReturn(session);
        when(sessionMapper.toDto(session)).thenReturn(updatedDto);

        mockMvc.perform(put("/api/session/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga modifié"));

        verify(sessionService).update(1L, session);
    }

    @Test
    @DisplayName("PUT /api/session/{id} devrait retourner 400 si champs requis manquants")
    void shouldReturn400WhenUpdateFieldsMissing() throws Exception {
        SessionDto invalid = new SessionDto();

        mockMvc.perform(put("/api/session/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(sessionService, never()).update(any(), any());
    }

    // -------------------------------------------------------------------------
    // Suppression
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/session/{id} devrait supprimer la session")
    void shouldDeleteSession() throws Exception {
        doNothing().when(sessionService).delete(1L);

        mockMvc.perform(delete("/api/session/{id}", 1))
                .andExpect(status().isOk());

        verify(sessionService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/session/{id} devrait retourner 404 si session introuvable")
    void shouldReturn404WhenDeleteMissingSession() throws Exception {
        doThrow(new NotFoundException("Session not found")).when(sessionService).delete(99L);

        mockMvc.perform(delete("/api/session/{id}", 99))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // Participate
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/session/{id}/participate/{userId} devrait inscrire l'utilisateur")
    void shouldParticipate() throws Exception {
        doNothing().when(sessionService).participate(1L, 2L);

        mockMvc.perform(post("/api/session/{id}/participate/{userId}", 1, 2))
                .andExpect(status().isOk());

        verify(sessionService).participate(1L, 2L);
    }

    @Test
    @DisplayName("POST participate devrait retourner 400 si déjà inscrit")
    void shouldReturn400WhenAlreadyParticipating() throws Exception {
        doThrow(new BadRequestException("User already participates in this session"))
                .when(sessionService).participate(1L, 2L);

        mockMvc.perform(post("/api/session/{id}/participate/{userId}", 1, 2))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE participate devrait désinscrire l'utilisateur")
    void shouldNoLongerParticipate() throws Exception {
        doNothing().when(sessionService).noLongerParticipate(1L, 2L);

        mockMvc.perform(delete("/api/session/{id}/participate/{userId}", 1, 2))
                .andExpect(status().isOk());

        verify(sessionService).noLongerParticipate(1L, 2L);
    }
}
