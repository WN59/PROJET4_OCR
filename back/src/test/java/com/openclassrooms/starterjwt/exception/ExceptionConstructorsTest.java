package com.openclassrooms.starterjwt.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Couvre les constructeurs sans message (souvent oubliés par JaCoCo).
 */
@DisplayName("Exceptions métier (constructeurs)")
class ExceptionConstructorsTest {

    @Test
    @DisplayName("constructeurs no-arg et avec message")
    void shouldInstantiateExceptions() {
        assertThat(new NotFoundException()).isInstanceOf(RuntimeException.class);
        assertThat(new NotFoundException("x")).hasMessage("x");

        assertThat(new BadRequestException()).isInstanceOf(RuntimeException.class);
        assertThat(new BadRequestException("y")).hasMessage("y");

        assertThat(new UnauthorizedException()).isInstanceOf(RuntimeException.class);
        assertThat(new UnauthorizedException("z")).hasMessage("z");
    }
}
