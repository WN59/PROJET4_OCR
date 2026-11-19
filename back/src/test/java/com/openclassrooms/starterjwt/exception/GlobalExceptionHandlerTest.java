package com.openclassrooms.starterjwt.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Couvre les branches restantes du handler (ex. BadRequest sans message).
 */
@DisplayName("GlobalExceptionHandler (unitaire)")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BadRequestException sans message → 400 sans corps")
    void shouldHandleBadRequestWithoutMessage() {
        ResponseEntity<?> response = handler.handleBadRequestException(new BadRequestException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("BadRequestException avec message → 400 avec corps")
    void shouldHandleBadRequestWithMessage() {
        ResponseEntity<?> response =
                handler.handleBadRequestException(new BadRequestException("déjà pris"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("autres handlers devraient renvoyer le bon statut")
    void shouldHandleOtherExceptions() {
        assertThat(handler.handleNumberFormatException(new NumberFormatException()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(handler.handleNotFoundException(new NotFoundException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleUnauthorizedException(new UnauthorizedException()).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(handler.handleBadCredentialsException(new BadCredentialsException("bad")).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
