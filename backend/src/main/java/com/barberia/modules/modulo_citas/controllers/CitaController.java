package com.barberia.modules.modulo_citas.controllers;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Cita>>> obtenerTodas() {
        List<Cita> citas = citaRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas", citas));
    }
    @GetMapping("/buscar/{noCita}")
    public ResponseEntity<ApiResponse<Cita>> obtenerPorId(@PathVariable Long noCita) {
        return citaRepository.findById(noCita)
                .map(c -> ResponseEntity.ok(ApiResponse.success("Cita obtenida", c)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Cita no encontrada")));
    }
}
