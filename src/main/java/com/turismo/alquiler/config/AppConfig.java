package com.turismo.alquiler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public OpenAPI alquilerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API de Alquiler de Bicicletas")
                .description("Sistema de gestión de alquiler de bicicletas urbanas. " +
                    "Permite registrar bicicletas, iniciar y finalizar alquileres, " +
                    "calcular costos automáticamente según tipo y aplicar multas por retraso.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Jacobo Solarte")
                    .email("contacto@turismo.com"))
                .license(new License()
                    .name("Prueba Técnica")));
    }
}
