package com.barberia.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Barberia Service API")
                        .version("1.0.0")
                        .description("API REST para gestión de barbería - Clientes y Servicios")
                        .contact(new Contact()
                                .name("Equipo Barberia")
                                .email("soporte@barberia.local")));
    }
}
