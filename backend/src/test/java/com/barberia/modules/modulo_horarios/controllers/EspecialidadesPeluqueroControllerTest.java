package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.dto.ActualizarEspecialidadesPeluqueroDTO;
import com.barberia.modules.modulo_horarios.services.EspecialidadesPeluqueroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialidadesPeluqueroControllerTest {

    @Mock
    private EspecialidadesPeluqueroService especialidadesPeluqueroService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private EspecialidadesPeluqueroController especialidadesPeluqueroController;

    @BeforeEach
    void setUp() {
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "456"));
    }

    @Test
    void actualizarEspecialidades_exitoso() {
        ActualizarEspecialidadesPeluqueroDTO dto = new ActualizarEspecialidadesPeluqueroDTO();
        dto.setIdServicio(1L);

        doNothing().when(especialidadesPeluqueroService).asociarServicio(anyString(), anyLong());

        ResponseEntity<String> result = especialidadesPeluqueroController.actualizarEspecialidades(dto, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Servicio asociado correctamente al peluquero", result.getBody());
    }

    @Test
    void obtenerNumeroDocumento_desdeDetails() {
        ActualizarEspecialidadesPeluqueroDTO dto = new ActualizarEspecialidadesPeluqueroDTO();
        dto.setIdServicio(1L);

        especialidadesPeluqueroController.actualizarEspecialidades(dto, authentication);

        verify(especialidadesPeluqueroService).asociarServicio("456", 1L);
    }
}
