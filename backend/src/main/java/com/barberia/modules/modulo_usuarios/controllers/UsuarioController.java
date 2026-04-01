package com.barberia.modules.modulo_usuarios.controllers;

import com.barberia.modules.modulo_usuarios.models.dtos.RegistroDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.services.UsuarioService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * POST /api/personas/registro
     * Registra una nueva persona con contraseña
     * Requiere: numeroDocumento, numeroCelular, email, nombrePersona, contraseña, confirmarContraseña
     */
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<UsuarioDTO>> registrarPersona(@RequestBody RegistroDTO registroDTO) {
        try {
            UsuarioDTO personaCreada = usuarioService.registrarPersona(registroDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Persona registrada exitosamente", personaCreada));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/personas/{id}
     * Obtiene una persona por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioDTO>> obtenerPorId(@PathVariable Long id) {
        try {
            UsuarioDTO persona = usuarioService.obtenerPorId(id);
            return ResponseEntity.ok(ApiResponse.success("Persona obtenida", persona));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/personas/documento/{numeroDocumento}
     * Obtiene una persona por número de documento
     */
    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<ApiResponse<UsuarioDTO>> obtenerPorDocumento(@PathVariable String numeroDocumento) {
        try {
            UsuarioDTO persona = usuarioService.obtenerPorNumeroDocumento(numeroDocumento);
            return ResponseEntity.ok(ApiResponse.success("Persona obtenida", persona));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/personas/email/{email}
     * Obtiene una persona por email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UsuarioDTO>> obtenerPorEmail(@PathVariable String email) {
        try {
            UsuarioDTO persona = usuarioService.obtenerPorEmail(email);
            return ResponseEntity.ok(ApiResponse.success("Persona obtenida", persona));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/personas
     * Obtiene todas las personas
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> obtenerTodas() {
        List<UsuarioDTO> personas = usuarioService.obtenerTodas();
        return ResponseEntity.ok(ApiResponse.success("Personas obtenidas", personas));
    }
}

