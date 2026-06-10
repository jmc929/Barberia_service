package com.barberia.modules.modulo_agendamiento.controllers;

import com.barberia.modules.modulo_agendamiento.models.dtos.*;
import com.barberia.modules.modulo_agendamiento.services.CitaAgendamientoService;
import com.barberia.shared.utils.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamientoControllerTest {

    @Mock
    private CitaAgendamientoService citaAgendamientoService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AgendamientoController agendamientoController;

    private CitaDisponibilidadRequestDTO disponibilidadRequest;
    private CitaDisponibilidadResponseDTO disponibilidadResponse;
    private CitaDTO citaDTO;

    @BeforeEach
    void setUp() {
        disponibilidadRequest = CitaDisponibilidadRequestDTO.builder()
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 6, 15))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .build();

        disponibilidadResponse = CitaDisponibilidadResponseDTO.builder()
                .disponible(true)
                .mensaje("Disponible")
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 6, 15))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .build();

        citaDTO = CitaDTO.builder()
                .noCita(1L)
                .numeroDocumentoCliente("123")
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .build();
    }

    private void setupAuth() {
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
    }

    @Test
    void consultarDisponibilidad_exitoso() {
        when(citaAgendamientoService.consultarDisponibilidad(any()))
                .thenReturn(disponibilidadResponse);

        ResponseEntity<ApiResponse<CitaDisponibilidadResponseDTO>> result =
                agendamientoController.consultarDisponibilidad(disponibilidadRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void consultarDisponibilidad_cuandoIllegalArgument_retorna400() {
        when(citaAgendamientoService.consultarDisponibilidad(any()))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<CitaDisponibilidadResponseDTO>> result =
                agendamientoController.consultarDisponibilidad(disponibilidadRequest);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void consultarDisponibilidad_cuandoExcepcionGeneral_retorna500() {
        when(citaAgendamientoService.consultarDisponibilidad(any()))
                .thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<ApiResponse<CitaDisponibilidadResponseDTO>> result =
                agendamientoController.consultarDisponibilidad(disponibilidadRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    void agendar_exitoso() {
        setupAuth();
        CitaCreateDTO createDTO = CitaCreateDTO.builder()
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 6, 15))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .build();
        when(citaAgendamientoService.agendar(any(), anyString())).thenReturn(citaDTO);

        ResponseEntity<ApiResponse<org.springframework.hateoas.EntityModel<CitaDTO>>> result =
                agendamientoController.agendar(createDTO, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void agendar_cuandoIllegalArgument_retorna400() {
        setupAuth();
        CitaCreateDTO createDTO = CitaCreateDTO.builder()
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 6, 15))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .build();
        when(citaAgendamientoService.agendar(any(), anyString()))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<org.springframework.hateoas.EntityModel<CitaDTO>>> result =
                agendamientoController.agendar(createDTO, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void cancelar_exitoso() {
        setupAuth();
        CancelarCitaDTO cancelDTO = CancelarCitaDTO.builder()
                .motivoCancelacion("Motivo")
                .build();
        when(citaAgendamientoService.cancelar(anyLong(), anyString(), anyString()))
                .thenReturn(citaDTO);

        ResponseEntity<ApiResponse<CitaDTO>> result =
                agendamientoController.cancelar(1L, cancelDTO, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void cancelar_cuandoIllegalArgument_retorna400() {
        setupAuth();
        CancelarCitaDTO cancelDTO = CancelarCitaDTO.builder()
                .motivoCancelacion("Motivo")
                .build();
        when(citaAgendamientoService.cancelar(anyLong(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<CitaDTO>> result =
                agendamientoController.cancelar(1L, cancelDTO, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void cancelar_cuandoExcepcionGeneral_retorna404() {
        setupAuth();
        CancelarCitaDTO cancelDTO = CancelarCitaDTO.builder()
                .motivoCancelacion("Motivo")
                .build();
        when(citaAgendamientoService.cancelar(anyLong(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Error"));

        ResponseEntity<ApiResponse<CitaDTO>> result =
                agendamientoController.cancelar(1L, cancelDTO, authentication);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void confirmar_exitoso() {
        setupAuth();
        when(citaAgendamientoService.confirmar(anyLong(), anyString())).thenReturn(citaDTO);

        ResponseEntity<ApiResponse<CitaDTO>> result =
                agendamientoController.confirmar(1L, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void confirmar_cuandoIllegalArgument_retorna400() {
        setupAuth();
        when(citaAgendamientoService.confirmar(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<CitaDTO>> result =
                agendamientoController.confirmar(1L, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void reprogramar_exitoso() {
        setupAuth();
        CitaReprogramarDTO reproDTO = CitaReprogramarDTO.builder()
                .fechaCita(LocalDate.of(2026, 6, 16))
                .horaInicioCita(LocalTime.of(10, 0))
                .horaFinCita(LocalTime.of(11, 0))
                .build();
        when(citaAgendamientoService.reprogramar(anyLong(), any(), anyString()))
                .thenReturn(citaDTO);

        ResponseEntity<ApiResponse<CitaDTO>> result =
                agendamientoController.reprogramar(1L, reproDTO, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void reprogramar_cuandoIllegalArgument_retorna400() {
        setupAuth();
        CitaReprogramarDTO reproDTO = CitaReprogramarDTO.builder()
                .fechaCita(LocalDate.of(2026, 6, 16))
                .horaInicioCita(LocalTime.of(10, 0))
                .horaFinCita(LocalTime.of(11, 0))
                .build();
        when(citaAgendamientoService.reprogramar(anyLong(), any(), anyString()))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<CitaDTO>> result =
                agendamientoController.reprogramar(1L, reproDTO, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void obtenerNumeroDocumento_desdeDetails() {
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));

        agendamientoController.confirmar(1L, authentication);

        verify(citaAgendamientoService).confirmar(1L, "123");
    }

    @Test
    void obtenerNumeroDocumento_desdeName() {
        when(authentication.getDetails()).thenReturn(Map.of());
        when(authentication.getName()).thenReturn("name-fallback");

        agendamientoController.confirmar(1L, authentication);

        verify(citaAgendamientoService).confirmar(1L, "name-fallback");
    }
}
