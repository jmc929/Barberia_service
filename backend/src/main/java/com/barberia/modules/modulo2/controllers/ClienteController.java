package com.barberia.modules.modulo2.controllers;

import com.barberia.modules.modulo2.models.dtos.ClienteDTO;
import com.barberia.modules.modulo2.services.ClienteService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Obtener todos los clientes
     * GET /api/clientes
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClienteDTO>>> obtenerTodos() {
        List<ClienteDTO> clientes = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(ApiResponse.success("Clientes obtenidos correctamente", clientes));
    }

    /**
     * Obtener un cliente por ID
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteDTO>> obtenerPorId(@PathVariable Long id) {
        ClienteDTO cliente = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(ApiResponse.success("Cliente obtenido correctamente", cliente));
    }

    /**
     * Buscar cliente por email
     * GET /api/clientes/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<ClienteDTO>> obtenerPorEmail(@PathVariable String email) {
        ClienteDTO cliente = clienteService.obtenerClientePorEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Cliente obtenido correctamente", cliente));
    }

    /**
     * Crear un nuevo cliente
     * POST /api/clientes
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ClienteDTO>> crear(@RequestBody ClienteDTO clienteDTO) {
        ClienteDTO clienteCreado = clienteService.crearCliente(clienteDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cliente creado correctamente", clienteCreado));
    }

    /**
     * Actualizar un cliente
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteDTO>> actualizar(
            @PathVariable Long id,
            @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO clienteActualizado = clienteService.actualizarCliente(id, clienteDTO);
        return ResponseEntity.ok(ApiResponse.success("Cliente actualizado correctamente", clienteActualizado));
    }

    /**
     * Desactivar un cliente
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.ok(ApiResponse.success("Cliente desactivado correctamente", null));
    }
}
