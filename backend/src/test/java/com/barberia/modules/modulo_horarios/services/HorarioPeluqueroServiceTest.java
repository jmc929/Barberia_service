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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
        List<ActualizarHorarioPeluqueroDTO.DiaHorario> dias = new ArrayList<>();
        ActualizarHorarioPeluqueroDTO.DiaHorario dia = new ActualizarHorarioPeluqueroDTO.DiaHorario();
        dia.setDiaSemana(DayOfWeek.MONDAY);
        dia.setHoraInicio(LocalTime.of(18, 0));
        dia.setHoraFin(LocalTime.of(8, 0));
        dias.add(dia);
        dto.setDias(dias);
        assertThrows(HorarioInvalidoException.class, () -> horarioPeluqueroService.actualizarHorario("123", dto));
    }

    @Test
    void testActualizarHorario_SolapamientoDias() {
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        List<ActualizarHorarioPeluqueroDTO.DiaHorario> dias = new ArrayList<>();
        ActualizarHorarioPeluqueroDTO.DiaHorario dia1 = new ActualizarHorarioPeluqueroDTO.DiaHorario();
        dia1.setDiaSemana(DayOfWeek.MONDAY);
        dia1.setHoraInicio(LocalTime.of(8, 0));
        dia1.setHoraFin(LocalTime.of(12, 0));
        ActualizarHorarioPeluqueroDTO.DiaHorario dia2 = new ActualizarHorarioPeluqueroDTO.DiaHorario();
        dia2.setDiaSemana(DayOfWeek.MONDAY);
        dia2.setHoraInicio(LocalTime.of(13, 0));
        dia2.setHoraFin(LocalTime.of(17, 0));
        dias.add(dia1);
        dias.add(dia2);
        dto.setDias(dias);
        assertThrows(HorarioInvalidoException.class, () -> horarioPeluqueroService.actualizarHorario("123", dto));
    }

    @Test
    void testActualizarHorario_Exito() {
        ActualizarHorarioPeluqueroDTO dto = new ActualizarHorarioPeluqueroDTO();
        List<ActualizarHorarioPeluqueroDTO.DiaHorario> dias = new ArrayList<>();
        ActualizarHorarioPeluqueroDTO.DiaHorario dia = new ActualizarHorarioPeluqueroDTO.DiaHorario();
        dia.setDiaSemana(DayOfWeek.TUESDAY);
        dia.setHoraInicio(LocalTime.of(9, 0));
        dia.setHoraFin(LocalTime.of(17, 0));
        dias.add(dia);
        dto.setDias(dias);
        when(horarioPeluqueroRepository.findByNumeroDocumentoPeluquero(anyString())).thenReturn(new ArrayList<>());
        assertDoesNotThrow(() -> horarioPeluqueroService.actualizarHorario("123", dto));
        verify(horarioPeluqueroRepository, times(1)).saveAll(anyList());
    }
}
