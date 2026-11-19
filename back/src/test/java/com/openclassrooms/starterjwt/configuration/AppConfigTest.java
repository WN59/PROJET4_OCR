package com.openclassrooms.starterjwt.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppConfig (unitaire)")
class AppConfigTest {

    @Test
    @DisplayName("propertySourcesPlaceholderConfigurer devrait charger .env")
    void shouldCreatePropertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer =
                AppConfig.propertySourcesPlaceholderConfigurer();

        assertThat(configurer).isNotNull();
    }
}
