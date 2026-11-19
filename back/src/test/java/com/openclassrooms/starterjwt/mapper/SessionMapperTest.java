package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.TeacherService;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Couvre {@link SessionMapperImpl} : résolution teacher/users via services mockés + branches null.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionMapper (unitaire)")
class SessionMapperTest {

    @Mock
    private TeacherService teacherService;

    @Mock
    private UserService userService;

    private SessionMapper mapper;

    private Teacher teacher;
    private User user;
    private Date sessionDate;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        SessionMapperImpl impl = new SessionMapperImpl();
        ReflectionTestUtils.setField(impl, "teacherService", teacherService);
        ReflectionTestUtils.setField(impl, "userService", userService);
        mapper = impl;

        teacher = Teacher.builder().id(1L).firstName("Margot").lastName("DELAHAYE").build();
        user = User.builder()
                .id(10L)
                .email("user@test.com")
                .firstName("John")
                .lastName("Doe")
                .password("pwd")
                .admin(false)
                .build();
        sessionDate = new Date();
        now = LocalDateTime.of(2024, 3, 1, 9, 0);
    }

    @Test
    @DisplayName("toEntity devrait résoudre teacher et users via les services")
    void shouldMapDtoToEntity() {
        SessionDto dto = new SessionDto(
                5L, "Yoga", sessionDate, 1L, "Desc", List.of(10L), now, now);
        when(teacherService.getById(1L)).thenReturn(teacher);
        when(userService.getById(10L)).thenReturn(user);

        Session entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getName()).isEqualTo("Yoga");
        assertThat(entity.getTeacher()).isEqualTo(teacher);
        assertThat(entity.getUsers()).containsExactly(user);
        verify(teacherService).getById(1L);
        verify(userService).getById(10L);
    }

    @Test
    @DisplayName("toEntity devrait accepter users null (liste vide)")
    void shouldMapDtoWithNullUsers() {
        SessionDto dto = new SessionDto(
                null, "Yoga", sessionDate, 1L, "Desc", null, null, null);
        when(teacherService.getById(1L)).thenReturn(teacher);

        Session entity = mapper.toEntity(dto);

        assertThat(entity.getUsers()).isEmpty();
        assertThat(entity.getTeacher()).isEqualTo(teacher);
    }

    @Test
    @DisplayName("toDto devrait exposer teacher_id et les ids users")
    void shouldMapEntityToDto() {
        Session session = Session.builder()
                .id(5L)
                .name("Yoga")
                .date(sessionDate)
                .description("Desc")
                .teacher(teacher)
                .users(List.of(user))
                .createdAt(now)
                .updatedAt(now)
                .build();

        SessionDto dto = mapper.toDto(session);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getTeacher_id()).isEqualTo(1L);
        assertThat(dto.getUsers()).containsExactly(10L);
        assertThat(dto.getName()).isEqualTo("Yoga");
    }

    @Test
    @DisplayName("toDto devrait gérer teacher null et users null")
    void shouldMapEntityWithNullTeacherAndUsers() {
        Session session = Session.builder()
                .id(5L)
                .name("Yoga")
                .date(sessionDate)
                .description("Desc")
                .teacher(null)
                .users(null)
                .build();

        SessionDto dto = mapper.toDto(session);

        assertThat(dto.getTeacher_id()).isNull();
        assertThat(dto.getUsers()).isEmpty();
    }

    @Test
    @DisplayName("toEntity / toDto devraient renvoyer null si entrée null")
    void shouldReturnNullWhenInputNull() {
        assertThat(mapper.toEntity((SessionDto) null)).isNull();
        assertThat(mapper.toDto((Session) null)).isNull();
    }

    @Test
    @DisplayName("toEntity(List) / toDto(List) devraient mapper une liste")
    void shouldMapLists() {
        SessionDto dto = new SessionDto(
                5L, "Yoga", sessionDate, 1L, "Desc", Collections.emptyList(), now, now);
        when(teacherService.getById(1L)).thenReturn(teacher);

        List<Session> entities = mapper.toEntity(List.of(dto));
        assertThat(entities).hasSize(1);

        List<SessionDto> dtos = mapper.toDto(entities);
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getTeacher_id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("toEntity(List) / toDto(List) devraient renvoyer null si liste null")
    void shouldReturnNullWhenListNull() {
        assertThat(mapper.toEntity((List<SessionDto>) null)).isNull();
        assertThat(mapper.toDto((List<Session>) null)).isNull();
    }
}
