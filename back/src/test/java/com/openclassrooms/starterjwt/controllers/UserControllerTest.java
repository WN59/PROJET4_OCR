package com.openclassrooms.starterjwt.controllers;

/**
 * Plan OCR — Account (affichage infos user) + suppression compte.
 * Logout = surtout front (clear token) ; pas d'endpoint dédié back.
 */

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.exception.UnauthorizedException;
import com.openclassrooms.starterjwt.mapper.UserMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController (intégration web) — Account")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("yoga@studio.com")
                .firstName("Admin")
                .lastName("Admin")
                .password("encoded")
                .admin(true)
                .build();

        userDto = new UserDto(
                1L,
                "yoga@studio.com",
                "Admin",
                "Admin",
                true,
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("GET /api/user/{id} devrait retourner les infos du compte")
    void shouldReturnUserAccount() throws Exception {
        when(userService.getById(1L)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        mockMvc.perform(get("/api/user/{id}", 1).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("yoga@studio.com"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("Admin"))
                .andExpect(jsonPath("$.admin").value(true));

        verify(userService).getById(1L);
    }

    @Test
    @DisplayName("GET /api/user/{id} devrait retourner 404 si utilisateur introuvable")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getById(99L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/user/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "yoga@studio.com")
    @DisplayName("DELETE /api/user/{id} devrait supprimer son propre compte")
    void shouldDeleteOwnAccount() throws Exception {
        doNothing().when(userService).deleteAccount(1L, "yoga@studio.com");

        mockMvc.perform(delete("/api/user/{id}", 1))
                .andExpect(status().isOk());

        verify(userService).deleteAccount(1L, "yoga@studio.com");
    }

    @Test
    @WithMockUser(username = "hacker@studio.com")
    @DisplayName("DELETE /api/user/{id} devrait retourner 401 si on cible un autre compte")
    void shouldReturn401WhenDeletingAnotherAccount() throws Exception {
        doThrow(new UnauthorizedException("You are not allowed to delete this account"))
                .when(userService).deleteAccount(1L, "hacker@studio.com");

        mockMvc.perform(delete("/api/user/{id}", 1))
                .andExpect(status().isUnauthorized());
    }
}
