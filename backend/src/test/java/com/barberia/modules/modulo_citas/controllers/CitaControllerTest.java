package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaControllerTest {

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private CitaController citaController;

    private Cita cita;

    @BeforeEach
    void setUp() {
        cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoCliente("123")
                .numeroDocumentoPeluquero("456")
                .idServicio(1L)
                .fechaCita(LocalDate.of(2026, 6, 15))
                .horaInicioCita(LocalTime.of(9, 0))
                .horaFinCita(LocalTime.of(10, 0))
                .idEstado(1L)
                .build();
    }

    private void setupAuth() {
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
    }

    @Test
    void obtenerTodas_exitoso() {
        when(citaRepository.findAll()).thenReturn(List.of(cita));

        ResponseEntity<ApiResponse<List<Cita>>> result = citaController.obtenerTodas();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
        assertEquals(1, result.getBody().getData().size());
    }

    @Test
    void obtenerPorId_exitoso() {
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        ResponseEntity<ApiResponse<org.springframework.hateoas.EntityModel<Cita>>> result =
                citaController.obtenerPorId(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_retorna404() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse<org.springframework.hateoas.EntityModel<Cita>>> result =
                citaController.obtenerPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void historialClienteAdmin_exitoso() {
        when(citaRepository.findByNumeroDocumentoCliente("123")).thenReturn(List.of(cita));

        ResponseEntity<ApiResponse<org.springframework.hateoas.CollectionModel<org.springframework.hateoas.EntityModel<Cita>>>> result =
                citaController.historialClienteAdmin("123");

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void miHistorial_exitoso() {
        setupAuth();
        when(citaRepository.findByNumeroDocumentoCliente("123")).thenReturn(List.of(cita));

        ResponseEntity<ApiResponse<org.springframework.hateoas.CollectionModel<org.springframework.hateoas.EntityModel<Cita>>>> result =
                citaController.miHistorial(authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void obtenerNumeroDocumento_desdeDetails() {
        setupAuth();
        when(citaRepository.findByNumeroDocumentoCliente("123")).thenReturn(List.of(cita));

        citaController.miHistorial(authentication);

        verify(citaRepository).findByNumeroDocumentoCliente("123");
    }
}
