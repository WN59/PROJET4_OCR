package com.openclassrooms.starterjwt.controllers;

/**
 * Plan OCR — Login / Register (côté back API).
 * Intégration web : @WebMvcTest + MockMvc + @MockitoBean(AuthService).
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController (intégration web) — Login / Register")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/login devrait retourner 200 + JWT si credentials OK")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("yoga@studio.com");
        request.setPassword("test!1234");

        when(authService.login(any(LoginRequest.class))).thenReturn(
                new JwtResponse("fake-jwt", 1L, "yoga@studio.com", "Admin", "Admin", true)
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt"))
                .andExpect(jsonPath("$.username").value("yoga@studio.com"))
                .andExpect(jsonPath("$.admin").value(true));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login devrait échouer si mauvais login/password")
    void shouldFailLoginWithBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("yoga@studio.com");
        request.setPassword("wrong");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login devrait retourner 400 si champs requis manquants")
    void shouldReturn400WhenLoginFieldsMissing() throws Exception {
        LoginRequest request = new LoginRequest(); // email/password null

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/register devrait créer le compte et retourner 200")
    void shouldRegisterSuccessfully() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setEmail("new@studio.com");
        request.setFirstName("Yoga");
        request.setLastName("Studio");
        request.setPassword("test!1234");

        when(authService.register(any(SignupRequest.class)))
                .thenReturn(new MessageResponse("User registered successfully!"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        verify(authService).register(any(SignupRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register devrait retourner 400 si champs requis manquants")
    void shouldReturn400WhenRegisterFieldsMissing() throws Exception {
        SignupRequest request = new SignupRequest(); // tout vide

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/register devrait retourner 400 si email déjà pris")
    void shouldReturn400WhenEmailAlreadyTaken() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setEmail("yoga@studio.com");
        request.setFirstName("Yoga");
        request.setLastName("Studio");
        request.setPassword("test!1234");

        when(authService.register(any(SignupRequest.class)))
                .thenThrow(new BadRequestException("Error: Email is already taken!"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already taken!"));
    }
}
