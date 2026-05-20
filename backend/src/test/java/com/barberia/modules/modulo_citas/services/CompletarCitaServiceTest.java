package com.barberia.modules.modulo_citas.services;

import com.barberia.modules.modulo_citas.dto.CompletarCitaRequestDTO;
import com.barberia.modules.modulo_citas.dto.CompletarCitaResponseDTO;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Map;

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
                .idEstado(5L)
                .build();
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
        CompletarCitaResponseDTO response = completarCitaService.completarCita(request, authentication);
        assertEquals("5", response.getEstadoAnterior());
        assertEquals(6L, cita.getIdEstado());
        assertEquals("COMPLETADA", response.getEstadoActual());
    }

    @Test
    void completarCita_noEsPeluqueroAsignado() {
        Cita cita = Cita.builder()
                .noCita(2L)
                .numeroDocumentoPeluquero("999")
                .idEstado(1L)
                .build();
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(2L);
        when(citaRepository.findById(2L)).thenReturn(Optional.of(cita));
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
        assertThrows(SecurityException.class, () -> completarCitaService.completarCita(request, authentication));
    }

    @Test
    void completarCita_estadoInvalido() {
        Cita cita = Cita.builder()
                .noCita(3L)
                .numeroDocumentoPeluquero("123")
                .idEstado(4L)
                .build();
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(3L);
        when(citaRepository.findById(3L)).thenReturn(Optional.of(cita));
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
        CompletarCitaResponseDTO response = completarCitaService.completarCita(request, authentication);
        assertEquals("4", response.getEstadoAnterior());
    }

    @Test
    void completarCita_citaNoExiste() {
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(99L);
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
        assertThrows(IllegalArgumentException.class, () -> completarCitaService.completarCita(request, authentication));
    }
}
