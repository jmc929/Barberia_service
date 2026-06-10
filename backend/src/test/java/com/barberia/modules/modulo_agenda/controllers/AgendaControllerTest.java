package com.barberia.modules.modulo_agenda.controllers;

import com.barberia.modules.modulo_agenda.models.dtos.AgendaResponseDTO;
import com.barberia.modules.modulo_agenda.services.AgendaService;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendaControllerTest {

    @Mock
    private AgendaService agendaService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AgendaController agendaController;

    @BeforeEach
    void setUp() {
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "456"));
    }

    @Test
    void miAgenda_exitoso() {
        AgendaResponseDTO agendaItem = AgendaResponseDTO.builder()
                .noCita(1L)
                .fecha(LocalDate.of(2026, 6, 15))
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(10, 0))
                .nombreCliente("Cliente Test")
                .idServicio(1L)
                .nombreServicio("Corte")
                .estado("ACTIVO")
                .build();

        when(agendaService.obtenerAgendaPeluquero("456")).thenReturn(List.of(agendaItem));

        ResponseEntity<ApiResponse<org.springframework.hateoas.CollectionModel<org.springframework.hateoas.EntityModel<AgendaResponseDTO>>>> result =
                agendaController.miAgenda(authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void obtenerNumeroDocumento_desdeDetails() {
        when(agendaService.obtenerAgendaPeluquero("456")).thenReturn(List.of());

        agendaController.miAgenda(authentication);

        verify(agendaService).obtenerAgendaPeluquero("456");
    }
}
