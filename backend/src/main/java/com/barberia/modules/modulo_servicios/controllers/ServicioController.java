package com.barberia.modules.modulo_servicios.controllers;

import com.barberia.modules.modulo_servicios.models.dtos.ServicioDTO;
import com.barberia.modules.modulo_servicios.services.ServicioService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioDTO>>> obtenerTodos() {
        List<ServicioDTO> servicios = servicioService.obtenerTodos();
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos", servicios));
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<ServicioDTO>> obtenerPorId(@PathVariable Long id) {
        try {
            ServicioDTO servicio = servicioService.obtenerPorId(id);
            return ResponseEntity.ok(ApiResponse.success("Servicio obtenido", servicio));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/crear")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<ServicioDTO>> crear(@RequestBody ServicioDTO servicioDTO) {
        try {
            ServicioDTO creado = servicioService.crear(servicioDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Servicio creado exitosamente", creado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<ServicioDTO>> actualizar(
            @PathVariable Long id,
            @RequestBody ServicioDTO servicioDTO) {
        try {
            ServicioDTO actualizado = servicioService.actualizar(id, servicioDTO);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Servicio actualizado exitosamente",
                            actualizado
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        try {
            servicioService.eliminar(id);

            return ResponseEntity.ok(
                    ApiResponse.success("Servicio eliminado exitosamente", null)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/deshabilitar/{id}")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<ServicioDTO>> deshabilitar(@PathVariable Long id) {
        try {
            ServicioDTO servicio = servicioService.deshabilitar(id);

            return ResponseEntity.ok(
                    ApiResponse.success("Servicio deshabilitado exitosamente", servicio)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
