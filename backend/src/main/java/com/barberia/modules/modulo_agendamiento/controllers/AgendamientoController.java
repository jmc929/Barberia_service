package com.barberia.modules.modulo_agendamiento.controllers;

import com.barberia.modules.modulo_agendamiento.models.dtos.*;
import com.barberia.modules.modulo_agendamiento.services.CitaAgendamientoService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agendamiento")
public class AgendamientoController {

    private final CitaAgendamientoService citaAgendamientoService;

    public AgendamientoController(CitaAgendamientoService citaAgendamientoService) {
        this.citaAgendamientoService = citaAgendamientoService;
    }

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
            String numeroDocumentoCliente = obtenerNumeroDocumento(authentication);
            CitaDTO creado = citaAgendamientoService.agendar(request, numeroDocumentoCliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Cita agendada", creado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cancelar/{noCita}")
    public ResponseEntity<ApiResponse<CitaDTO>> cancelar(@PathVariable Long noCita,
                                                        @RequestBody CancelarCitaDTO request,
                                                        Authentication authentication) {
        try {
            String numeroDocumentoSolicitante = obtenerNumeroDocumento(authentication);
            CitaDTO cancelada = citaAgendamientoService.cancelar(noCita, request.getMotivoCancelacion(), numeroDocumentoSolicitante);
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
            String numeroDocumentoPeluquero = obtenerNumeroDocumento(authentication);
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
                                                            @RequestBody CitaReprogramarDTO request,
                                                            Authentication authentication) {
        try {
            String numeroDocumentoCliente = obtenerNumeroDocumento(authentication);
            CitaDTO reprogramada = citaAgendamientoService.reprogramar(noCita, request, numeroDocumentoCliente);
            return ResponseEntity.ok(ApiResponse.success("Cita reprogramada", reprogramada));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
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
