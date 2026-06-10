package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.dto.ActualizarHorarioPeluqueroDTO;
import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import com.barberia.modules.modulo_horarios.services.HorarioPeluqueroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioPeluqueroControllerTest {

    @Mock
    private HorarioPeluqueroService horarioPeluqueroService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private HorarioPeluqueroController horarioPeluqueroController;

    @BeforeEach
    void setUp() {
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "456"));
    }

    @Test
    void actualizarHorario_exitoso() {
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        dto.setIdDia(2L);
        dto.setHoraInicioHorario("09:00:00");
        dto.setHoraFinHorario("17:00:00");

        doNothing().when(horarioPeluqueroService).actualizarHorario(anyString(), any());

        ResponseEntity<String> result = horarioPeluqueroController.actualizarHorario(dto, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Horario actualizado correctamente", result.getBody());
    }

    @Test
    void obtenerHorario_exitoso() {
        HorarioPeluquero horario = new HorarioPeluquero();
        horario.setNumeroDocumentoPeluquero("456");
        horario.setIdDia(2L);
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(17, 0));

        when(horarioPeluqueroService.obtenerHorario("456")).thenReturn(List.of(horario));

        ResponseEntity<List<HorarioPeluquero>> result = horarioPeluqueroController.obtenerHorario(authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void obtenerNumeroDocumento_desdeDetails() {
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        dto.setIdDia(2L);
        dto.setHoraInicioHorario("09:00:00");
        dto.setHoraFinHorario("17:00:00");

        horarioPeluqueroController.actualizarHorario(dto, authentication);

        verify(horarioPeluqueroService).actualizarHorario("456", dto);
    }
}
