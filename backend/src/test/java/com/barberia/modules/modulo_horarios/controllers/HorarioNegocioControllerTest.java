package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioUpdateDTO;
import com.barberia.modules.modulo_horarios.services.HorarioNegocioService;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import com.barberia.shared.utils.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioNegocioControllerTest {

    @Mock
    private HorarioNegocioService horarioNegocioService;

    @InjectMocks
    private HorarioNegocioController horarioNegocioController;

    private HorarioNegocioDTO horarioDTO;

    @BeforeEach
    void setUp() {
        horarioDTO = HorarioNegocioDTO.builder()
                .idHorarioNegocio(1L)
                .idDia(2L)
                .horaApertura(LocalTime.of(7, 0))
                .horaCierre(LocalTime.of(17, 0))
                .localAbierto(true)
                .build();
    }

    @Test
    void obtenerTodos_exitoso() {
        when(horarioNegocioService.obtenerTodos()).thenReturn(List.of(horarioDTO));

        ResponseEntity<ApiResponse<List<HorarioNegocioDTO>>> result = horarioNegocioController.obtenerTodos();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
        assertEquals(1, result.getBody().getData().size());
    }

    @Test
    void obtenerPorDia_exitoso() {
        when(horarioNegocioService.obtenerPorDia(2L)).thenReturn(horarioDTO);

        ResponseEntity<ApiResponse<HorarioNegocioDTO>> result = horarioNegocioController.obtenerPorDia(2L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void obtenerPorDia_cuandoNoExiste_retorna404() {
        when(horarioNegocioService.obtenerPorDia(99L))
                .thenThrow(new ResourceNotFoundException("No encontrado"));

        ResponseEntity<ApiResponse<HorarioNegocioDTO>> result = horarioNegocioController.obtenerPorDia(99L);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void actualizarHorario_exitoso() {
        HorarioUpdateDTO updateDTO = new HorarioUpdateDTO();
        updateDTO.setHoraApertura("08:00:00");
        updateDTO.setHoraCierre("18:00:00");
        when(horarioNegocioService.actualizarHorario(anyLong(), any())).thenReturn(horarioDTO);

        ResponseEntity<ApiResponse<HorarioNegocioDTO>> result =
                horarioNegocioController.actualizarHorario(2L, updateDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void actualizarHorario_cuandoIllegalArgument_retorna400() {
        HorarioUpdateDTO updateDTO = new HorarioUpdateDTO();
        when(horarioNegocioService.actualizarHorario(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<HorarioNegocioDTO>> result =
                horarioNegocioController.actualizarHorario(2L, updateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void actualizarHorario_cuandoExcepcionGeneral_retorna404() {
        HorarioUpdateDTO updateDTO = new HorarioUpdateDTO();
        when(horarioNegocioService.actualizarHorario(anyLong(), any()))
                .thenThrow(new RuntimeException("Error"));

        ResponseEntity<ApiResponse<HorarioNegocioDTO>> result =
                horarioNegocioController.actualizarHorario(2L, updateDTO);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void cerrarDia_exitoso() {
        when(horarioNegocioService.cerrarDia(2L)).thenReturn(horarioDTO);

        ResponseEntity<ApiResponse<HorarioNegocioDTO>> result = horarioNegocioController.cerrarDia(2L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void cerrarDia_cuandoError_retorna404() {
        when(horarioNegocioService.cerrarDia(99L))
                .thenThrow(new ResourceNotFoundException("No encontrado"));

        ResponseEntity<ApiResponse<HorarioNegocioDTO>> result = horarioNegocioController.cerrarDia(99L);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}
