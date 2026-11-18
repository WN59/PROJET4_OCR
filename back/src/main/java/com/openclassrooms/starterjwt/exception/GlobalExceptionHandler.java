package com.openclassrooms.starterjwt.exception;

import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire global des exceptions de l'API.
 * <p>
 * Grâce à {@link RestControllerAdvice}, toutes les exceptions listées ici
 * sont interceptées quel que soit le controller, ce qui évite de répéter
 * des {@code try/catch} partout.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Id non numérique dans l'URL (ex. {@code /api/session/abc}).
     *
     * @param exception exception levée lors du parse Long
     * @return HTTP 400
     */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Void> handleNumberFormatException(NumberFormatException exception) {
        return ResponseEntity.badRequest().build();
    }

    /**
     * Ressource introuvable (user, session, teacher...).
     *
     * @param exception exception métier not found
     * @return HTTP 404
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Void> handleNotFoundException(NotFoundException exception) {
        return ResponseEntity.notFound().build();
    }

    /**
     * Requête invalide (email déjà pris, déjà participant, etc.).
     * <p>
     * Si un message est présent, il est renvoyé dans un {@link MessageResponse}.
     *
     * @param exception exception métier bad request
     * @return HTTP 400 (avec ou sans corps)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException exception) {
        if (StringUtils.hasText(exception.getMessage())) {
            return ResponseEntity.badRequest().body(new MessageResponse(exception.getMessage()));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Accès non autorisé (ex. supprimer le compte d'un autre).
     *
     * @param exception exception métier unauthorized
     * @return HTTP 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Void> handleUnauthorizedException(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Login échoué (mauvais email / mot de passe).
     *
     * @param exception levée par Spring Security à l'authentification
     * @return HTTP 401
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
