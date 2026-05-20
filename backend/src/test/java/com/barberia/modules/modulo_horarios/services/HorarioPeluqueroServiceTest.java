package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.dto.ActualizarHorarioPeluqueroDTO;
import com.barberia.modules.modulo_horarios.exceptions.HorarioInvalidoException;
import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import com.barberia.modules.modulo_horarios.repositories.HorarioPeluqueroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para HorarioPeluqueroService.
 */
public class HorarioPeluqueroServiceTest {
    @Mock
    private HorarioPeluqueroRepository horarioPeluqueroRepository;

    @InjectMocks
    private HorarioPeluqueroService horarioPeluqueroService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testActualizarHorario_HoraInicioMayorQueFin() {
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        dto.setIdDia(1L);
        dto.setHoraInicioHorario("18:00:00");
        dto.setHoraFinHorario("08:00:00");
        assertThrows(HorarioInvalidoException.class, () -> horarioPeluqueroService.actualizarHorario("123", dto));
    }

    @Test
    void testActualizarHorario_Exito() {
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        dto.setIdDia(2L);
        dto.setHoraInicioHorario("09:00:00");
        dto.setHoraFinHorario("17:00:00");
        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluqueroAndIdDia(anyString(), anyLong()))
                .thenReturn(Optional.empty());
        assertDoesNotThrow(() -> horarioPeluqueroService.actualizarHorario("123", dto));
        verify(horarioPeluqueroRepository, times(1)).save(any(HorarioPeluquero.class));
    }
}
