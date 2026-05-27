package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.dto.ActualizarEspecialidadesPeluqueroDTO;
import com.barberia.modules.modulo_horarios.services.EspecialidadesPeluqueroService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import java.util.Map;

/**
 * Controlador REST para la actualización de especialidades del peluquero.
 */
@RestController
@RequestMapping("/api/v1/especialidades-peluqueros")
@Validated
public class EspecialidadesPeluqueroController {
    private final EspecialidadesPeluqueroService especialidadesPeluqueroService;

    public EspecialidadesPeluqueroController(EspecialidadesPeluqueroService especialidadesPeluqueroService) {
        this.especialidadesPeluqueroService = especialidadesPeluqueroService;
    }

    /**
     * Endpoint para asociar un servicio al peluquero autenticado.
     */
    @PutMapping("/actualizar")
    @PreAuthorize("hasAuthority('ROLE_2')")
    public ResponseEntity<?> actualizarEspecialidades(@Valid @RequestBody ActualizarEspecialidadesPeluqueroDTO dto, Authentication authentication) {
        String numeroDocumentoPeluquero = obtenerNumeroDocumento(authentication);
        especialidadesPeluqueroService.asociarServicio(numeroDocumentoPeluquero, dto.getIdServicio());
        return ResponseEntity.ok("Servicio asociado correctamente al peluquero");
    }

    /**
     * Obtiene el numeroDocumento desde los detalles del JWT.
     */
    @SuppressWarnings("unchecked")
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
