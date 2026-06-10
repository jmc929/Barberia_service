package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.dto.ActualizarHorarioPeluqueroDTO;
import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import com.barberia.modules.modulo_horarios.services.HorarioPeluqueroService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import java.util.Map;
import java.util.List;

/**
 * Controlador REST para la gestión del horario laboral del peluquero.
 */
@RestController
@RequestMapping("/api/v1/horarios-peluqueros")
@Validated
public class HorarioPeluqueroController {
    private final HorarioPeluqueroService horarioPeluqueroService;

    public HorarioPeluqueroController(HorarioPeluqueroService horarioPeluqueroService) {
        this.horarioPeluqueroService = horarioPeluqueroService;
    }

    /**
     * Endpoint para actualizar el horario laboral del peluquero autenticado.
     */
    @PutMapping("/actualizar")
    @PreAuthorize("hasAuthority('ROLE_2')")
    public ResponseEntity<String> actualizarHorario(@Valid @RequestBody ActualizarHorarioPeluqueroDTO dto, Authentication authentication) {
        String numeroDocumentoPeluquero = obtenerNumeroDocumento(authentication);
        horarioPeluqueroService.actualizarHorario(numeroDocumentoPeluquero, dto);
        return ResponseEntity.ok("Horario actualizado correctamente");
    }

    /**
     * Endpoint para consultar el horario laboral del peluquero autenticado.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_2')")
    public ResponseEntity<List<HorarioPeluquero>> obtenerHorario(Authentication authentication) {
        String numeroDocumentoPeluquero = obtenerNumeroDocumento(authentication);
        List<HorarioPeluquero> horario = horarioPeluqueroService.obtenerHorario(numeroDocumentoPeluquero);
        return ResponseEntity.ok(horario);
    }

    private String obtenerNumeroDocumento(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> detailsMap) {
            Object numeroDocumento = detailsMap.get("numeroDocumento");
            if (numeroDocumento != null) {
                return numeroDocumento.toString();
            }
        }
        return authentication.getName();
    }
}
