package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.dto.ActualizarHorarioPeluqueroDTO;
import com.barberia.modules.modulo_horarios.exceptions.HorarioInvalidoException;
import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import com.barberia.modules.modulo_horarios.repositories.HorarioPeluqueroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioPeluqueroServiceTest {
    @Mock
    private HorarioPeluqueroRepository horarioPeluqueroRepository;

    @InjectMocks
    private HorarioPeluqueroService horarioPeluqueroService;

    private ActualizarHorarioPeluqueroDTO dtoValido;

    @BeforeEach
    void setUp() {
        dtoValido = new ActualizarHorarioPeluqueroDTO();
        dtoValido.setIdDia(2L);
        dtoValido.setHoraInicioHorario("09:00:00");
        dtoValido.setHoraFinHorario("17:00:00");
    }

    @Test
    void actualizarHorario_HoraInicioMayorQueFin_lanzaExcepcion() {
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        dto.setIdDia(1L);
        dto.setHoraInicioHorario("18:00:00");
        dto.setHoraFinHorario("08:00:00");
        assertThrows(HorarioInvalidoException.class, () -> horarioPeluqueroService.actualizarHorario("123", dto));
    }

    @Test
    void actualizarHorario_HoraInicioIgualAFin_lanzaExcepcion() {
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        dto.setIdDia(1L);
        dto.setHoraInicioHorario("10:00:00");
        dto.setHoraFinHorario("10:00:00");
        assertThrows(HorarioInvalidoException.class, () -> horarioPeluqueroService.actualizarHorario("123", dto));
    }

    @Test
    void actualizarHorario_nuevo_exitoso() {
        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluqueroAndIdDia(anyString(), anyLong()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> horarioPeluqueroService.actualizarHorario("123", dtoValido));
        verify(horarioPeluqueroRepository, times(1)).save(any(HorarioPeluquero.class));
    }

    @Test
    void actualizarHorario_existente_exitoso() {
        HorarioPeluquero existente = new HorarioPeluquero();
        existente.setNumeroDocumentoPeluquero("123");
        existente.setIdDia(2L);
        existente.setHoraInicio(LocalTime.of(8, 0));
        existente.setHoraFin(LocalTime.of(16, 0));

        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluqueroAndIdDia(anyString(), anyLong()))
                .thenReturn(Optional.of(existente));

        assertDoesNotThrow(() -> horarioPeluqueroService.actualizarHorario("123", dtoValido));
        verify(horarioPeluqueroRepository, times(1)).save(any(HorarioPeluquero.class));
        assertEquals(LocalTime.of(9, 0), existente.getHoraInicio());
        assertEquals(LocalTime.of(17, 0), existente.getHoraFin());
    }

    @Test
    void obtenerHorario_exitoso() {
        HorarioPeluquero horario = new HorarioPeluquero();
        horario.setNumeroDocumentoPeluquero("123");
        horario.setIdDia(2L);

        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluquero("123")).thenReturn(List.of(horario));

        List<HorarioPeluquero> result = horarioPeluqueroService.obtenerHorario("123");

        assertEquals(1, result.size());
        assertEquals("123", result.get(0).getNumeroDocumentoPeluquero());
    }

    @Test
    void obtenerHorario_sinHorarios_retornaListaVacia() {
        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluquero("999")).thenReturn(List.of());

        List<HorarioPeluquero> result = horarioPeluqueroService.obtenerHorario("999");

        assertTrue(result.isEmpty());
    }
}
