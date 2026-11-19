package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Couvre génération / lecture / validation JWT (succès + branches d'erreur).
 * Utilise l'API jjwt 0.12 (sans méthodes dépréciées).
 */
@DisplayName("JwtUtils (unitaire)")
class JwtUtilsTest {

    /** Secret Base64 ≥ 512 bits (exigence HS512 de jjwt). */
    private static final String SECRET =
            "kaZQMOmMwzrIWa1lsuMlOdTm04x8c5Pgf3Pu29JkZEqUZwV8UTegHOb1+eN4Sa1aGTk7zTAaqungoev5e5Hg6g==";
    private static final String OTHER_SECRET =
            "HRTetq2cehJL8EDBkH7pAchlrAYiZlODnxa28ctNOD0SmgMw1rxfn14ZRGqJOCIeOCx63mMPIlq9U/4mz+2OMA==";
    private static final int EXPIRATION_MS = 86_400_000;

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", EXPIRATION_MS);
    }

    private static SecretKey key(String base64Secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    @Test
    @DisplayName("generateJwtToken + getUserNameFromJwtToken + validate devraient fonctionner")
    void shouldGenerateParseAndValidateToken() {
        UserDetailsImpl principal = UserDetailsImpl.builder()
                .id(1L)
                .username("yoga@studio.com")
                .firstName("Admin")
                .lastName("Admin")
                .admin(true)
                .password("pwd")
                .build();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String token = jwtUtils.generateJwtToken(authentication);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("yoga@studio.com");
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateJwtToken devrait renvoyer false si signature invalide")
    void shouldRejectInvalidSignature() {
        String token = Jwts.builder()
                .subject("yoga@studio.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key(OTHER_SECRET), Jwts.SIG.HS512)
                .compact();

        assertThat(jwtUtils.validateJwtToken(token)).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken devrait renvoyer false si token malformé")
    void shouldRejectMalformedToken() {
        assertThat(jwtUtils.validateJwtToken("pas.un.jwt")).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken devrait renvoyer false si token expiré")
    void shouldRejectExpiredToken() {
        String token = Jwts.builder()
                .subject("yoga@studio.com")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 5_000))
                .signWith(key(SECRET), Jwts.SIG.HS512)
                .compact();

        assertThat(jwtUtils.validateJwtToken(token)).isFalse();
    }

    @Test
    @DisplayName("validateJwtToken devrait renvoyer false si claims vides")
    void shouldRejectEmptyClaims() {
        assertThat(jwtUtils.validateJwtToken("")).isFalse();
    }
}
