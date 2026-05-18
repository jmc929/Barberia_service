package com.barberia.modules.modulo_usuarios.controllers;

import com.barberia.modules.modulo_usuarios.models.dtos.RegistroDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.services.UsuarioService;
import com.barberia.shared.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.barberia.modules.modulo_usuarios.models.dtos.UpdatePerfilDTO;
import org.springframework.security.core.Authentication;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/personas")
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
     * GET /api/personas/documento/{numeroDocumento}
     * Obtiene una persona por número de documento
     * Requiere: Rol Administrador (idRol = 1)
     */
    @GetMapping("/documento/{numeroDocumento}")
    @PreAuthorize("hasAuthority('ROLE_1')")
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
     * Requiere: Rol Administrador (idRol = 1)
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('ROLE_1')")
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
     * GET /api/personas/barberos
     * Obtiene todos los usuarios con rol barbero (idRol = 2)
     * Requiere: Rol Administrador (idRol = 1)
     */
    @GetMapping("/barberos")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> obtenerBarberos() {
        List<UsuarioDTO> barberos = usuarioService.obtenerBarberos();
        return ResponseEntity.ok(ApiResponse.success("Barberos obtenidos", barberos));
    }

    /**
     * GET /api/personas
     * Obtiene todas las personas
     * Requiere: Rol Administrador (idRol = 1)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> obtenerTodas() {
        List<UsuarioDTO> personas = usuarioService.obtenerTodas();
        return ResponseEntity.ok(ApiResponse.success("Personas obtenidas", personas));
    }

    /**
     * PUT /api/personas/perfil
     * Actualiza el perfil del usuario autenticado. Solo puede actualizar su propio perfil.
     */
    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioDTO>> actualizarPerfil(@RequestBody UpdatePerfilDTO request,
                                                                     Authentication authentication) {
        try {
            @SuppressWarnings("unchecked")
            String numeroDocumento = (String) ((Map<String, Object>) authentication.getDetails()).get("numeroDocumento");
            UsuarioDTO actualizado = usuarioService.actualizarPerfil(numeroDocumento, request);
            return ResponseEntity.ok(ApiResponse.success("Perfil actualizado exitosamente", actualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/personas/{numeroDocumento}/rol
     * Cambia el rol de un usuario
     * Requiere: Rol Administrador (idRol = 1)
     */
    @PutMapping("/{numeroDocumento}/rol")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> cambiarRol(
            @PathVariable String numeroDocumento,
            @RequestParam Integer nuevoRol) {
        try {
            UsuarioDTO usuario = usuarioService.cambiarRol(numeroDocumento, nuevoRol);
            return ResponseEntity.ok(ApiResponse.success("Rol actualizado exitosamente", usuario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/personas/{numeroDocumento}/bloquear
     * Bloquea un cliente (rol 3) cambiando su idEstado a 4
     * Requiere: Rol Administrador (idRol = 1)
     */
    @PutMapping("/{numeroDocumento}/bloquear")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> bloquearUsuario(
            @PathVariable String numeroDocumento) {
        try {
            UsuarioDTO usuario = usuarioService.bloquearUsuario(numeroDocumento);
            return ResponseEntity.ok(ApiResponse.success("Cliente bloqueado exitosamente", usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/personas/bloqueados
     * Obtiene todos los clientes bloqueados (idEstado = 4)
     * Requiere: Rol Administrador (idRol = 1)
     */
    @GetMapping("/bloqueados")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> obtenerUsuariosBloqueados() {
        List<UsuarioDTO> usuariosBloqueados = usuarioService.obtenerUsuariosBloqueados();
        return ResponseEntity.ok(ApiResponse.success("Clientes bloqueados obtenidos", usuariosBloqueados));
    }

    /**
     * PUT /api/personas/{numeroDocumento}/desbloquear
     * Desbloquea un cliente (rol 3) cambiando su idEstado de 4 a 1
     * Requiere: Rol Administrador (idRol = 1)
     */
    @PutMapping("/{numeroDocumento}/desbloquear")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> desbloquearUsuario(
            @PathVariable String numeroDocumento) {
        try {
            UsuarioDTO usuario = usuarioService.desbloquearUsuario(numeroDocumento);
            return ResponseEntity.ok(ApiResponse.success("Cliente desbloqueado exitosamente", usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/personas/{numeroDocumento}/deshabilitar-barbero
     * Deshabilita un barbero (idRol = 2) cambiando su idEstado a 5
     * Requiere: Rol Administrador (idRol = 1)
     */
    @PutMapping("/{numeroDocumento}/deshabilitar-barbero")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> deshabilitarBarbero(
            @PathVariable String numeroDocumento) {
        try {
            UsuarioDTO usuario = usuarioService.deshabilitarBarbero(numeroDocumento);
            return ResponseEntity.ok(ApiResponse.success("Barbero deshabilitado exitosamente", usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/personas/{numeroDocumento}/habilitar-barbero
     * Habilita un barbero deshabilitado (idRol = 2, idEstado = 5) cambiando su idEstado a 1
     * Requiere: Rol Administrador (idRol = 1)
     */
    @PutMapping("/{numeroDocumento}/habilitar-barbero")
    @PreAuthorize("hasAuthority('ROLE_1')")
    public ResponseEntity<ApiResponse<UsuarioDTO>> habilitarBarbero(
            @PathVariable String numeroDocumento) {
        try {
            UsuarioDTO usuario = usuarioService.habilitarBarbero(numeroDocumento);
            return ResponseEntity.ok(ApiResponse.success("Barbero habilitado exitosamente", usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}

