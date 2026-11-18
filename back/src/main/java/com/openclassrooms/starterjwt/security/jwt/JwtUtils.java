package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utilitaire JWT : génération, lecture et validation des tokens.
 * <p>
 * Le secret et la durée de vie viennent de {@code application.yml}
 * ({@code oc.app.jwtSecret} / {@code oc.app.jwtExpirationMs}).
 * <p>
 * API jjwt 0.12 : {@code subject}/{@code verifyWith}/{@code parseSignedClaims}
 * (les anciennes méthodes {@code setSubject}/{@code setSigningKey} sont dépréciées).
 */
@Slf4j
@Component
public class JwtUtils {
    @Value("${oc.app.jwtSecret}")
    private String jwtSecret;

    @Value("${oc.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Clé HMAC dérivée du secret Base64 (exigence HS512 : ≥ 512 bits).
     *
     * @return clé de signature / vérification
     */
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Génère un JWT signé pour l'utilisateur authentifié.
     * <p>
     * Le subject du token est l'email (username).
     *
     * @param authentication résultat de l'authentification Spring Security
     * @return token JWT compact (string)
     */
    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Extrait l'email (subject) contenu dans le token.
     *
     * @param token JWT
     * @return email / username
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Vérifie que le token est bien formé, signé et non expiré.
     *
     * @param authToken JWT à valider
     * @return {@code true} si valide, sinon {@code false}
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
