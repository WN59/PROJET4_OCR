package com.openclassrooms.starterjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'application Spring Boot Yoga App.
 * <p>
 * L'audit JPA ({@code createdAt}/{@code updatedAt}) est activé dans
 * {@link com.openclassrooms.starterjwt.configuration.JpaConfig} (séparé
 * pour ne pas casser les {@code @WebMvcTest}).
 */
@SpringBootApplication
public class SpringBootSecurityJwtApplication {

    /**
     * Démarre le contexte Spring (API + Docker Compose MySQL si configuré).
     *
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootSecurityJwtApplication.class, args);
    }
}
