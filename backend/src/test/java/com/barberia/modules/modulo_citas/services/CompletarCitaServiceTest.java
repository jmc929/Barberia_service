package com.barberia.modules.modulo_citas.services;

import com.barberia.modules.modulo_citas.dto.CompletarCitaRequestDTO;
import com.barberia.modules.modulo_citas.dto.CompletarCitaResponseDTO;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompletarCitaServiceTest {
    @Mock
    private CitaRepository citaRepository;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private CompletarCitaService completarCitaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void completarCita_exito() {
        Cita cita = Cita.builder()
                .noCita(1L)
                .numeroDocumentoPeluquero("123")
                .idEstado((long) EstadoCita.valueOf("CONFIRMADA").ordinal())
                .build();
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(authentication.getName()).thenReturn("123");
        CompletarCitaResponseDTO response = completarCitaService.completarCita(request, authentication);
        assertEquals("COMPLETADA", response.getEstadoActual());
        assertNotNull(response.getFechaCompletado());
    }

    @Test
    void completarCita_noEsPeluqueroAsignado() {
        Cita cita = Cita.builder()
                .noCita(2L)
                .numeroDocumentoPeluquero("999")
                .idEstado((long) EstadoCita.valueOf("CONFIRMADA").ordinal())
                .build();
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(2L);
        when(citaRepository.findById(2L)).thenReturn(Optional.of(cita));
        when(authentication.getName()).thenReturn("123");
        assertThrows(SecurityException.class, () -> completarCitaService.completarCita(request, authentication));
    }

    @Test
    void completarCita_estadoInvalido() {
        Cita cita = Cita.builder()
                .noCita(3L)
                .numeroDocumentoPeluquero("123")
                .idEstado((long) EstadoCita.valueOf("CANCELADA").ordinal())
                .build();
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(3L);
        when(citaRepository.findById(3L)).thenReturn(Optional.of(cita));
        when(authentication.getName()).thenReturn("123");
        assertThrows(IllegalStateException.class, () -> completarCitaService.completarCita(request, authentication));
    }

    @Test
    void completarCita_citaNoExiste() {
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(99L);
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());
        when(authentication.getName()).thenReturn("123");
        assertThrows(IllegalArgumentException.class, () -> completarCitaService.completarCita(request, authentication));
    }
}
