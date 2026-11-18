package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService (unitaire)")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("yoga@studio.com")
                .firstName("Admin")
                .lastName("Admin")
                .admin(true)
                .password("encoded")
                .build();
    }

    @AfterEach
    void tearDown() {
        // login() remplit le SecurityContext : on le nettoie après chaque test
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("login devrait authentifier et retourner un JwtResponse")
    void shouldLoginAndReturnJwtResponse() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("yoga@studio.com");
        loginRequest.setPassword("test!1234");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("fake-jwt-token");

        JwtResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("yoga@studio.com");
        assertThat(response.getFirstName()).isEqualTo("Admin");
        assertThat(response.getLastName()).isEqualTo("Admin");
        assertThat(response.getAdmin()).isTrue();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
    }

    @Test
    @DisplayName("register devrait hasher le mot de passe et déléguer à UserService")
    void shouldRegisterAndReturnSuccessMessage() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("new@studio.com");
        signupRequest.setFirstName("Yoga");
        signupRequest.setLastName("Studio");
        signupRequest.setPassword("test!1234");

        when(passwordEncoder.encode("test!1234")).thenReturn("encoded-pwd");
        when(userService.register(
                eq("new@studio.com"),
                eq("Studio"),
                eq("Yoga"),
                eq("encoded-pwd")
        )).thenReturn(User.builder()
                .id(3L)
                .email("new@studio.com")
                .firstName("Yoga")
                .lastName("Studio")
                .password("encoded-pwd")
                .admin(false)
                .build());

        MessageResponse response = authService.register(signupRequest);

        assertThat(response.getMessage()).isEqualTo("User registered successfully!");
        verify(passwordEncoder).encode("test!1234");
        verify(userService).register("new@studio.com", "Studio", "Yoga", "encoded-pwd");
    }
}
