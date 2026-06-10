package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.dto.CompletarCitaRequestDTO;
import com.barberia.modules.modulo_citas.dto.CompletarCitaResponseDTO;
import com.barberia.modules.modulo_citas.services.CompletarCitaService;
import com.barberia.shared.utils.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompletarCitaControllerTest {

    @Mock
    private CompletarCitaService completarCitaService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private CompletarCitaController completarCitaController;

    @Test
    void completarCita_exitoso() {
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(1L);
        CompletarCitaResponseDTO response = CompletarCitaResponseDTO.builder()
                .idCita(1L)
                .estadoAnterior("5")
                .estadoActual("COMPLETADA")
                .mensaje("Cita completada exitosamente")
                .build();
        when(completarCitaService.completarCita(request, authentication)).thenReturn(response);

        ResponseEntity<ApiResponse<CompletarCitaResponseDTO>> result =
                completarCitaController.completarCita(request, authentication);

        assertTrue(result.getBody().isSuccess());
        assertEquals(1L, result.getBody().getData().getIdCita().longValue());
    }

    @Test
    void completarCita_cuandoError_lanzaExcepcion() {
        CompletarCitaRequestDTO request = new CompletarCitaRequestDTO();
        request.setIdCita(1L);
        when(completarCitaService.completarCita(request, authentication))
                .thenThrow(new SecurityException("No tienes permiso"));

        assertThrows(SecurityException.class,
                () -> completarCitaController.completarCita(request, authentication));
    }
}
