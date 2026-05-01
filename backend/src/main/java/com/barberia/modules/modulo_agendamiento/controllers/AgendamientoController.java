package com.barberia.modules.modulo_agendamiento.controllers;

import com.barberia.modules.modulo_agendamiento.models.dtos.*;
import com.barberia.modules.modulo_agendamiento.services.CitaAgendamientoService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agendamiento")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AgendamientoController {

    @Autowired
    private CitaAgendamientoService citaAgendamientoService;

    @PostMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<CitaDisponibilidadResponseDTO>> consultarDisponibilidad(
            @RequestBody CitaDisponibilidadRequestDTO request) {
        try {
            CitaDisponibilidadResponseDTO resp = citaAgendamientoService.consultarDisponibilidad(request);
            return ResponseEntity.ok(ApiResponse.success("Disponibilidad consultada", resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/agendar")
    public ResponseEntity<ApiResponse<CitaDTO>> agendar(@RequestBody CitaCreateDTO request,
                                                        Authentication authentication) {
        try {
            @SuppressWarnings("unchecked")
            String numeroDocumentoCliente = (String) ((Map<String, Object>) authentication.getDetails())
                    .get("numeroDocumento");
            CitaDTO creado = citaAgendamientoService.agendar(request, numeroDocumentoCliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Cita agendada", creado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cancelar/{noCita}")
    public ResponseEntity<ApiResponse<CitaDTO>> cancelar(@PathVariable Long noCita) {
        try {
            CitaDTO cancelada = citaAgendamientoService.cancelar(noCita);
            return ResponseEntity.ok(ApiResponse.success("Cita cancelada", cancelada));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/confirmar/{noCita}")
    public ResponseEntity<ApiResponse<CitaDTO>> confirmar(@PathVariable Long noCita,
                                                          Authentication authentication) {
        try {
            @SuppressWarnings("unchecked")
            String numeroDocumentoPeluquero = (String) ((Map<String, Object>) authentication.getDetails())
                    .get("numeroDocumento");
            CitaDTO confirmada = citaAgendamientoService.confirmar(noCita, numeroDocumentoPeluquero);
            return ResponseEntity.ok(ApiResponse.success("Cita confirmada", confirmada));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/reprogramar/{noCita}")
    public ResponseEntity<ApiResponse<CitaDTO>> reprogramar(@PathVariable Long noCita,
                                                            @RequestBody CitaReprogramarDTO request) {
        try {
            CitaDTO reprogramada = citaAgendamientoService.reprogramar(noCita, request);
            return ResponseEntity.ok(ApiResponse.success("Cita reprogramada", reprogramada));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
