package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.dto.CompletarCitaRequestDTO;
import com.barberia.modules.modulo_citas.dto.CompletarCitaResponseDTO;
import com.barberia.modules.modulo_citas.services.CompletarCitaService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/citas")
public class CompletarCitaController {
    @Autowired
    private CompletarCitaService completarCitaService;

    /**
     * Endpoint para que el barbero (idRol = 2) marque una cita como completada.
     */
    @PostMapping("/completar")
    @PreAuthorize("hasAuthority('ROLE_2')")
    public ResponseEntity<ApiResponse<CompletarCitaResponseDTO>> completarCita(@RequestBody CompletarCitaRequestDTO request, Authentication authentication) {
        CompletarCitaResponseDTO response = completarCitaService.completarCita(request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Cita completada", response));
    }
}
