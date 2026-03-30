package com.barberia.modules.modulo1.controllers;

import com.barberia.modules.modulo1.models.dtos.ServicioDTO;
import com.barberia.modules.modulo1.services.ServicioService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    /**
     * Obtener todos los servicios
     * GET /api/servicios
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioDTO>>> obtenerTodos() {
        List<ServicioDTO> servicios = servicioService.obtenerTodosLosServicios();
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos correctamente", servicios));
    }

    /**
     * Obtener un servicio por ID
     * GET /api/servicios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioDTO>> obtenerPorId(@PathVariable Long id) {
        ServicioDTO servicio = servicioService.obtenerServicioPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Servicio obtenido correctamente", servicio));
    }

    /**
     * Crear un nuevo servicio
     * POST /api/servicios
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServicioDTO>> crear(@RequestBody ServicioDTO servicioDTO) {
        ServicioDTO servicioCreado = servicioService.crearServicio(servicioDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Servicio creado correctamente", servicioCreado));
    }

    /**
     * Actualizar un servicio
     * PUT /api/servicios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioDTO>> actualizar(
            @PathVariable Long id,
            @RequestBody ServicioDTO servicioDTO) {
        ServicioDTO servicioActualizado = servicioService.actualizarServicio(id, servicioDTO);
        return ResponseEntity.ok(ApiResponse.success("Servicio actualizado correctamente", servicioActualizado));
    }

    /**
     * Desactivar un servicio (eliminación lógica)
     * DELETE /api/servicios/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        servicioService.desactivarServicio(id);
        return ResponseEntity.ok(ApiResponse.success("Servicio desactivado correctamente", null));
    }
}
