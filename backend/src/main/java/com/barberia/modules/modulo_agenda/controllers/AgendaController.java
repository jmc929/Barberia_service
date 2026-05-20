package com.barberia.modules.modulo_agenda.controllers;

import com.barberia.modules.modulo_agenda.models.dtos.AgendaResponseDTO;
import com.barberia.modules.modulo_agenda.services.AgendaService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agenda")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AgendaController {
    @Autowired
    private AgendaService agendaService;

    /**
     * Agenda completa para administradores.
     */
    @GetMapping("/todas")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<AgendaResponseDTO>>> agendaCompleta() {
        List<AgendaResponseDTO> agenda = agendaService.obtenerAgendaCompleta();
        return ResponseEntity.ok(ApiResponse.success("Agenda completa obtenida", agenda));
    }

    /**
     * Agenda del peluquero autenticado.
     */
    @GetMapping("/mis-citas")
    @PreAuthorize("hasAuthority('ROLE_PELUQUERO')")
    public ResponseEntity<ApiResponse<List<AgendaResponseDTO>>> miAgenda(Authentication authentication) {
        String numeroDocumento = (String) authentication.getName();
        List<AgendaResponseDTO> agenda = agendaService.obtenerAgendaPeluquero(numeroDocumento);
        return ResponseEntity.ok(ApiResponse.success("Agenda obtenida", agenda));
    }
}
