package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.dto.ActualizarEspecialidadesPeluqueroDTO;
import com.barberia.modules.modulo_horarios.services.EspecialidadesPeluqueroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;

/**
 * Controlador REST para la actualización de especialidades del peluquero.
 */
@RestController
@RequestMapping("/api/v1/especialidades-peluquero")
@Validated
public class EspecialidadesPeluqueroController {
    @Autowired
    private EspecialidadesPeluqueroService especialidadesPeluqueroService;

    /**
     * Endpoint para actualizar las especialidades del peluquero autenticado.
     */
    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizarEspecialidades(@Valid @RequestBody ActualizarEspecialidadesPeluqueroDTO dto, Principal principal) {
        Long peluqueroId = obtenerIdDesdePrincipal(principal);
        especialidadesPeluqueroService.actualizarEspecialidades(peluqueroId, dto);
        return ResponseEntity.ok("Especialidades actualizadas correctamente");
    }

    /**
     * Método auxiliar para obtener el ID del usuario autenticado.
     * Debes implementar la lógica real según tu sistema de autenticación.
     */
    private Long obtenerIdDesdePrincipal(Principal principal) {
        // TODO: Implementar obtención real del ID del usuario
        return Long.valueOf(principal.getName()); // Ajustar según tu sistema
    }
}
