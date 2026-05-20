package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.dto.ActualizarHorarioPeluqueroDTO;
import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import com.barberia.modules.modulo_horarios.services.HorarioPeluqueroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * Controlador REST para la gestión del horario laboral del peluquero.
 */
@RestController
@RequestMapping("/api/v1/horario-peluquero")
@Validated
public class HorarioPeluqueroController {
    @Autowired
    private HorarioPeluqueroService horarioPeluqueroService;

    /**
     * Endpoint para actualizar el horario laboral del peluquero autenticado.
     */
    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizarHorario(@Valid @RequestBody ActualizarHorarioPeluqueroDTO dto, Principal principal) {
        // Aquí deberías obtener el ID del peluquero autenticado desde el principal
        Long peluqueroId = obtenerIdDesdePrincipal(principal);
        horarioPeluqueroService.actualizarHorario(peluqueroId, dto);
        return ResponseEntity.ok("Horario actualizado correctamente");
    }

    /**
     * Endpoint para consultar el horario laboral del peluquero autenticado.
     */
    @GetMapping
    public ResponseEntity<List<HorarioPeluquero>> obtenerHorario(Principal principal) {
        Long peluqueroId = obtenerIdDesdePrincipal(principal);
        List<HorarioPeluquero> horario = horarioPeluqueroService.obtenerHorario(peluqueroId);
        return ResponseEntity.ok(horario);
    }

    /**
     * Método auxiliar para obtener el ID del usuario autenticado.
     * Debes implementar la lógica real según tu sistema de autenticación.
     */
    private Long obtenerIdDesdePrincipal(Principal principal) {
        // TODO: Implementar obtención real del ID del usuario
        // Por ejemplo, si principal.getName() es el username, buscar el usuario y devolver su ID
        return Long.valueOf(principal.getName()); // Ajustar según tu sistema
    }
}
