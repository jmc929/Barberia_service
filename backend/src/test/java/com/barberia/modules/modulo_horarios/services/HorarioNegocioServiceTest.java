package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioUpdateDTO;
import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
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
class HorarioNegocioServiceTest {

    @Mock
    private HorarioNegocioRepository horarioNegocioRepository;

    @InjectMocks
    private HorarioNegocioService horarioNegocioService;

    private HorarioNegocio horarioNegocio;
    private HorarioUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        horarioNegocio = new HorarioNegocio();
        horarioNegocio.setIdHorarioNegocio(1L);
        horarioNegocio.setIdDia(2L);
        horarioNegocio.setHoraApertura(LocalTime.of(7, 0));
        horarioNegocio.setHoraCierre(LocalTime.of(17, 0));
        horarioNegocio.setLocalAbierto(true);

        updateDTO = new HorarioUpdateDTO();
        updateDTO.setHoraApertura("08:00:00");
        updateDTO.setHoraCierre("18:00:00");
    }

    @Test
    void obtenerTodos_exitoso() {
        when(horarioNegocioRepository.findAllByOrderByIdDiaAsc()).thenReturn(List.of(horarioNegocio));

        List<HorarioNegocioDTO> result = horarioNegocioService.obtenerTodos();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getIdDia().longValue());
    }

    @Test
    void obtenerPorDia_exitoso() {
        when(horarioNegocioRepository.findByIdDia(2L)).thenReturn(Optional.of(horarioNegocio));

        HorarioNegocioDTO result = horarioNegocioService.obtenerPorDia(2L);

        assertNotNull(result);
        assertEquals(2L, result.getIdDia().longValue());
    }

    @Test
    void obtenerPorDia_cuandoNoExiste_lanzaExcepcion() {
        when(horarioNegocioRepository.findByIdDia(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> horarioNegocioService.obtenerPorDia(99L));
    }

    @Test
    void actualizarHorario_exitoso() {
        when(horarioNegocioRepository.findByIdDia(2L)).thenReturn(Optional.of(horarioNegocio));
        when(horarioNegocioRepository.save(any(HorarioNegocio.class))).thenReturn(horarioNegocio);

        HorarioNegocioDTO result = horarioNegocioService.actualizarHorario(2L, updateDTO);

        assertNotNull(result);
        assertEquals(LocalTime.of(8, 0), horarioNegocio.getHoraApertura());
        assertEquals(LocalTime.of(18, 0), horarioNegocio.getHoraCierre());
        assertTrue(horarioNegocio.getLocalAbierto());
    }

    @Test
    void actualizarHorario_cuandoNoExiste_lanzaExcepcion() {
        when(horarioNegocioRepository.findByIdDia(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> horarioNegocioService.actualizarHorario(99L, updateDTO));
    }

    @Test
    void actualizarHorario_cuandoHoraNula_lanzaExcepcion() {
        when(horarioNegocioRepository.findByIdDia(2L)).thenReturn(Optional.of(horarioNegocio));
        HorarioUpdateDTO dtoNulo = new HorarioUpdateDTO();

        assertThrows(IllegalArgumentException.class,
                () -> horarioNegocioService.actualizarHorario(2L, dtoNulo));
    }

    @Test
    void actualizarHorario_cuandoFormatoHoraInvalido_lanzaExcepcion() {
        when(horarioNegocioRepository.findByIdDia(2L)).thenReturn(Optional.of(horarioNegocio));
        HorarioUpdateDTO dtoInvalido = new HorarioUpdateDTO();
        dtoInvalido.setHoraApertura("invalido");
        dtoInvalido.setHoraCierre("18:00:00");

        assertThrows(IllegalArgumentException.class,
                () -> horarioNegocioService.actualizarHorario(2L, dtoInvalido));
    }

    @Test
    void actualizarHorario_cuandoCierreAntesDeApertura_lanzaExcepcion() {
        when(horarioNegocioRepository.findByIdDia(2L)).thenReturn(Optional.of(horarioNegocio));
        HorarioUpdateDTO dtoInvalido = new HorarioUpdateDTO();
        dtoInvalido.setHoraApertura("18:00:00");
        dtoInvalido.setHoraCierre("08:00:00");

        assertThrows(IllegalArgumentException.class,
                () -> horarioNegocioService.actualizarHorario(2L, dtoInvalido));
    }

    @Test
    void cerrarDia_exitoso() {
        when(horarioNegocioRepository.findByIdDia(2L)).thenReturn(Optional.of(horarioNegocio));
        when(horarioNegocioRepository.save(any(HorarioNegocio.class))).thenReturn(horarioNegocio);

        HorarioNegocioDTO result = horarioNegocioService.cerrarDia(2L);

        assertNotNull(result);
        assertFalse(horarioNegocio.getLocalAbierto());
    }

    @Test
    void cerrarDia_cuandoNoExiste_lanzaExcepcion() {
        when(horarioNegocioRepository.findByIdDia(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> horarioNegocioService.cerrarDia(99L));
    }
}
