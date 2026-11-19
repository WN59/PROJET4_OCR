package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Couvre {@link UserMapperImpl} (généré MapStruct) : nulls + mapping + listes.
 */
@DisplayName("UserMapper (unitaire)")
class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapperImpl();
    }

    @Test
    @DisplayName("toEntity / toDto devraient mapper tous les champs")
    void shouldMapEntityAndDto() {
        LocalDateTime now = LocalDateTime.of(2024, 2, 1, 12, 0);
        UserDto dto = new UserDto(
                1L, "yoga@studio.com", "Admin", "Admin", true, "secret", now, now);

        User entity = mapper.toEntity(dto);
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getEmail()).isEqualTo("yoga@studio.com");
        assertThat(entity.getLastName()).isEqualTo("Admin");
        assertThat(entity.getFirstName()).isEqualTo("Admin");
        assertThat(entity.isAdmin()).isTrue();
        assertThat(entity.getPassword()).isEqualTo("secret");
        assertThat(entity.getCreatedAt()).isEqualTo(now);

        UserDto back = mapper.toDto(entity);
        assertThat(back.getEmail()).isEqualTo("yoga@studio.com");
        assertThat(back.isAdmin()).isTrue();
        assertThat(back.getPassword()).isEqualTo("secret");
    }

    @Test
    @DisplayName("toEntity / toDto devraient renvoyer null si entrée null")
    void shouldReturnNullWhenInputNull() {
        assertThat(mapper.toEntity((UserDto) null)).isNull();
        assertThat(mapper.toDto((User) null)).isNull();
    }

    @Test
    @DisplayName("toEntity(List) / toDto(List) devraient mapper une liste")
    void shouldMapLists() {
        UserDto dto = new UserDto(
                2L, "user@test.com", "Doe", "John", false, "pwd", null, null);

        List<User> entities = mapper.toEntity(List.of(dto));
        assertThat(entities).hasSize(1);
        assertThat(entities.get(0).getFirstName()).isEqualTo("John");

        List<UserDto> dtos = mapper.toDto(entities);
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getEmail()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("toEntity(List) / toDto(List) devraient renvoyer null si liste null")
    void shouldReturnNullWhenListNull() {
        assertThat(mapper.toEntity((List<UserDto>) null)).isNull();
        assertThat(mapper.toDto((List<User>) null)).isNull();
    }

    @Test
    @DisplayName("toEntity(List) / toDto(List) devraient gérer une liste vide")
    void shouldMapEmptyLists() {
        assertThat(mapper.toEntity(Collections.emptyList())).isEmpty();
        assertThat(mapper.toDto(Collections.emptyList())).isEmpty();
    }
}
