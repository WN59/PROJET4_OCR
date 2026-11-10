package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.TeacherService;
import com.openclassrooms.starterjwt.services.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mapper MapStruct entre {@link SessionDto} et {@link Session}.
 * <p>
 * Plus complexe que User/Teacher car :
 * <ul>
 *   <li>{@code teacher_id} (Long dans le DTO) ↔ objet {@code Teacher} (entité)</li>
 *   <li>{@code users} (liste d'ids) ↔ liste d'entités {@code User}</li>
 * </ul>
 */
@Component
@Mapper(componentModel = "spring", uses = {UserService.class}, imports = {Arrays.class, Collectors.class, Session.class, User.class, Collections.class, Optional.class})
public abstract class SessionMapper implements EntityMapper<SessionDto, Session> {

    /** Service utilisé pour résoudre le professeur à partir de {@code teacher_id}. */
    @Autowired
    TeacherService teacherService;

    /** Service utilisé pour résoudre les participants à partir de leurs ids. */
    @Autowired
    UserService userService;

    /**
     * DTO → Entity : reconstruit le teacher et la liste des users.
     * <p>
     * Un {@code teacher_id} invalide ou absent lève {@code NotFoundException}
     * (plus de session créée sans professeur).
     *
     * @param sessionDto données reçues de l'API
     * @return entité Session
     */
    @Mappings({
            @Mapping(source = "description", target = "description"),
            @Mapping(target = "teacher", expression = "java(this.teacherService.getById(sessionDto.getTeacher_id()))"),
            @Mapping(target = "users", expression = "java(Optional.ofNullable(sessionDto.getUsers()).orElseGet(Collections::emptyList).stream().map(user_id -> this.userService.getById(user_id)).collect(Collectors.toList()))"),
    })
    public abstract Session toEntity(SessionDto sessionDto);


    /**
     * Entity → DTO : expose {@code teacher_id} et la liste des ids users.
     *
     * @param session entité issue de la BDD
     * @return DTO pour la réponse API
     */
    @Mappings({
            @Mapping(source = "description", target = "description"),
            @Mapping(source = "session.teacher.id", target = "teacher_id"),
            @Mapping(target = "users", expression = "java(Optional.ofNullable(session.getUsers()).orElseGet(Collections::emptyList).stream().map(u -> u.getId()).collect(Collectors.toList()))"),
    })
    public abstract SessionDto toDto(Session session);
}
