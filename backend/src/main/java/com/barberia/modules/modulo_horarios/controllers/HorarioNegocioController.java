package com.barberia.modules.modulo_horarios.controllers;

import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioUpdateDTO;
import com.barberia.modules.modulo_horarios.services.HorarioNegocioService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HorarioNegocioController {

    @Autowired
    private HorarioNegocioService horarioNegocioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HorarioNegocioDTO>>> obtenerTodos() {
        List<HorarioNegocioDTO> horarios = horarioNegocioService.obtenerTodos();
        return ResponseEntity.ok(ApiResponse.success("Horarios obtenidos", horarios));
    }

    @GetMapping("/buscar/{idDia}")
    public ResponseEntity<ApiResponse<HorarioNegocioDTO>> obtenerPorDia(@PathVariable Long idDia) {
        try {
            HorarioNegocioDTO horario = horarioNegocioService.obtenerPorDia(idDia);
            return ResponseEntity.ok(ApiResponse.success("Horario obtenido", horario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/actualizar/{idDia}")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<HorarioNegocioDTO>> actualizarHorario(
            @PathVariable Long idDia, @RequestBody HorarioUpdateDTO dto) {
        try {
            HorarioNegocioDTO actualizado = horarioNegocioService.actualizarHorario(idDia, dto);
            return ResponseEntity.ok(ApiResponse.success("Horario actualizado exitosamente", actualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/cerrar/{idDia}")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<HorarioNegocioDTO>> cerrarDia(@PathVariable Long idDia) {
        try {
            HorarioNegocioDTO cerrado = horarioNegocioService.cerrarDia(idDia);
            return ResponseEntity.ok(ApiResponse.success("Día marcado como cerrado", cerrado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
