package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final CitaRepository citaRepository;

    public CitaController(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<List<Cita>>> obtenerTodas() {
        List<Cita> citas = citaRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas", citas));
    }
    @GetMapping("/buscar/{noCita}")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<Cita>> obtenerPorId(@PathVariable Long noCita) {
        return citaRepository.findById(noCita)
                .map(c -> ResponseEntity.ok(ApiResponse.success("Cita obtenida", c)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Cita no encontrada")));
    }

    @GetMapping("/historial/{numeroDocumentoCliente}")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<List<Cita>>> historialClienteAdmin(
            @PathVariable String numeroDocumentoCliente) {

        List<Cita> citas = citaRepository.findByNumeroDocumentoCliente(numeroDocumentoCliente);

        return ResponseEntity.ok(
                ApiResponse.success("Historial de citas obtenido", citas)
        );
    }
    @GetMapping("/mi-historial")
    @PreAuthorize("hasAuthority('ROLE_3')")
    public ResponseEntity<ApiResponse<List<Cita>>> miHistorial(Authentication authentication) {
        String numeroDocumento = obtenerNumeroDocumento(authentication);

        List<Cita> citas = citaRepository.findByNumeroDocumentoCliente(numeroDocumento);

        return ResponseEntity.ok(
                ApiResponse.success("Historial de citas obtenido", citas)
        );
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
