package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.models.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Couvre {@link TeacherMapperImpl} (généré MapStruct) : nulls + mapping simple + listes.
 */
@DisplayName("TeacherMapper (unitaire)")
class TeacherMapperTest {

    private TeacherMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TeacherMapperImpl();
    }

    @Test
    @DisplayName("toEntity / toDto devraient mapper tous les champs")
    void shouldMapEntityAndDto() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 0);
        TeacherDto dto = new TeacherDto(1L, "DELAHAYE", "Margot", now, now);

        Teacher entity = mapper.toEntity(dto);
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getLastName()).isEqualTo("DELAHAYE");
        assertThat(entity.getFirstName()).isEqualTo("Margot");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);

        TeacherDto back = mapper.toDto(entity);
        assertThat(back.getId()).isEqualTo(1L);
        assertThat(back.getLastName()).isEqualTo("DELAHAYE");
        assertThat(back.getFirstName()).isEqualTo("Margot");
    }

    @Test
    @DisplayName("toEntity / toDto devraient renvoyer null si entrée null")
    void shouldReturnNullWhenInputNull() {
        assertThat(mapper.toEntity((TeacherDto) null)).isNull();
        assertThat(mapper.toDto((Teacher) null)).isNull();
    }

    @Test
    @DisplayName("toEntity(List) / toDto(List) devraient mapper une liste")
    void shouldMapLists() {
        TeacherDto dto = new TeacherDto(2L, "THIERCELIN", "Hélène", null, null);

        List<Teacher> entities = mapper.toEntity(List.of(dto));
        assertThat(entities).hasSize(1);
        assertThat(entities.get(0).getFirstName()).isEqualTo("Hélène");

        List<TeacherDto> dtos = mapper.toDto(entities);
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getLastName()).isEqualTo("THIERCELIN");
    }

    @Test
    @DisplayName("toEntity(List) / toDto(List) devraient renvoyer null si liste null")
    void shouldReturnNullWhenListNull() {
        assertThat(mapper.toEntity((List<TeacherDto>) null)).isNull();
        assertThat(mapper.toDto((List<Teacher>) null)).isNull();
    }

    @Test
    @DisplayName("toEntity(List) / toDto(List) devraient gérer une liste vide")
    void shouldMapEmptyLists() {
        assertThat(mapper.toEntity(Collections.emptyList())).isEmpty();
        assertThat(mapper.toDto(Collections.emptyList())).isEmpty();
    }
}
