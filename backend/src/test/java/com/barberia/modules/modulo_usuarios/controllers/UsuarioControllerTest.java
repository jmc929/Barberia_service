package com.barberia.modules.modulo_usuarios.controllers;

import com.barberia.modules.modulo_usuarios.models.dtos.RegistroDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UpdatePerfilDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.services.UsuarioService;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import com.barberia.shared.utils.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UsuarioController usuarioController;

    private UsuarioDTO usuarioDTO;
    private RegistroDTO registroDTO;
    private UpdatePerfilDTO updatePerfilDTO;

    @BeforeEach
    void setUp() {
        usuarioDTO = UsuarioDTO.builder()
                .numeroDocumento("123")
                .email("test@test.com")
                .nombrePersona("Test")
                .build();

        registroDTO = RegistroDTO.builder()
                .numeroDocumento("789")
                .email("nuevo@test.com")
                .nombrePersona("Nuevo")
                .contraseña("password123")
                .confirmarContraseña("password123")
                .build();

        updatePerfilDTO = UpdatePerfilDTO.builder()
                .nombrePersona("Actualizado")
                .build();
    }

    private void setupAuth() {
        when(authentication.getDetails()).thenReturn(Map.of("numeroDocumento", "123"));
    }

    @Test
    void registrarPersona_exitoso() {
        when(usuarioService.registrarPersona(any())).thenReturn(usuarioDTO);

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.registrarPersona(registroDTO);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void registrarPersona_cuandoError_retorna400() {
        when(usuarioService.registrarPersona(any()))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.registrarPersona(registroDTO);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void obtenerPorDocumento_exitoso() {
        when(usuarioService.obtenerPorNumeroDocumento("123")).thenReturn(usuarioDTO);

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.obtenerPorDocumento("123");

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void obtenerPorDocumento_cuandoNoExiste_retorna404() {
        when(usuarioService.obtenerPorNumeroDocumento("999"))
                .thenThrow(new ResourceNotFoundException("No encontrado"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.obtenerPorDocumento("999");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void obtenerPorEmail_exitoso() {
        when(usuarioService.obtenerPorEmail("test@test.com")).thenReturn(usuarioDTO);

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.obtenerPorEmail("test@test.com");

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void obtenerBarberos_exitoso() {
        when(usuarioService.obtenerBarberos()).thenReturn(List.of(usuarioDTO));

        ResponseEntity<ApiResponse<List<UsuarioDTO>>> result = usuarioController.obtenerBarberos();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getData().size());
    }

    @Test
    void obtenerTodas_exitoso() {
        when(usuarioService.obtenerTodas()).thenReturn(List.of(usuarioDTO));

        ResponseEntity<ApiResponse<List<UsuarioDTO>>> result = usuarioController.obtenerTodas();

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void actualizarPerfil_exitoso() {
        setupAuth();
        when(usuarioService.actualizarPerfil(anyString(), any())).thenReturn(usuarioDTO);

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.actualizarPerfil(updatePerfilDTO, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void actualizarPerfil_cuandoIllegalArgument_retorna400() {
        setupAuth();
        when(usuarioService.actualizarPerfil(anyString(), any()))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.actualizarPerfil(updatePerfilDTO, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void actualizarPerfil_cuandoExcepcionGeneral_retorna500() {
        setupAuth();
        when(usuarioService.actualizarPerfil(anyString(), any()))
                .thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.actualizarPerfil(updatePerfilDTO, authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    void cambiarRol_exitoso() {
        when(usuarioService.cambiarRol("123", 2)).thenReturn(usuarioDTO);

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.cambiarRol("123", 2);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void cambiarRol_cuandoError_retorna404() {
        when(usuarioService.cambiarRol("999", 2))
                .thenThrow(new ResourceNotFoundException("No encontrado"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.cambiarRol("999", 2);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void bloquearUsuario_exitoso() {
        when(usuarioService.bloquearUsuario("123")).thenReturn(usuarioDTO);

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.bloquearUsuario("123");

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void bloquearUsuario_cuandoIllegalArgument_retorna400() {
        when(usuarioService.bloquearUsuario("123"))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.bloquearUsuario("123");

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void bloquearUsuario_cuandoExcepcionGeneral_retorna404() {
        when(usuarioService.bloquearUsuario("999"))
                .thenThrow(new ResourceNotFoundException("No encontrado"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.bloquearUsuario("999");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void obtenerUsuariosBloqueados_exitoso() {
        when(usuarioService.obtenerUsuariosBloqueados()).thenReturn(List.of(usuarioDTO));

        ResponseEntity<ApiResponse<List<UsuarioDTO>>> result = usuarioController.obtenerUsuariosBloqueados();

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void desbloquearUsuario_exitoso() {
        when(usuarioService.desbloquearUsuario("123")).thenReturn(usuarioDTO);

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.desbloquearUsuario("123");

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void desbloquearUsuario_cuandoIllegalArgument_retorna400() {
        when(usuarioService.desbloquearUsuario("123"))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.desbloquearUsuario("123");

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void desbloquearUsuario_cuandoExcepcionGeneral_retorna404() {
        when(usuarioService.desbloquearUsuario("999"))
                .thenThrow(new ResourceNotFoundException("No encontrado"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.desbloquearUsuario("999");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void deshabilitarBarbero_exitoso() {
        when(usuarioService.deshabilitarBarbero("456")).thenReturn(usuarioDTO);

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.deshabilitarBarbero("456");

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deshabilitarBarbero_cuandoIllegalArgument_retorna400() {
        when(usuarioService.deshabilitarBarbero("456"))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.deshabilitarBarbero("456");

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void habilitarBarbero_exitoso() {
        when(usuarioService.habilitarBarbero("456")).thenReturn(usuarioDTO);

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.habilitarBarbero("456");

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void habilitarBarbero_cuandoIllegalArgument_retorna400() {
        when(usuarioService.habilitarBarbero("456"))
                .thenThrow(new IllegalArgumentException("Error"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.habilitarBarbero("456");

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void habilitarBarbero_cuandoExcepcionGeneral_retorna404() {
        when(usuarioService.habilitarBarbero("999"))
                .thenThrow(new ResourceNotFoundException("No encontrado"));

        ResponseEntity<ApiResponse<UsuarioDTO>> result = usuarioController.habilitarBarbero("999");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void obtenerNumeroDocumento_desdeDetails() {
        setupAuth();
        when(usuarioService.actualizarPerfil(anyString(), any())).thenReturn(usuarioDTO);

        usuarioController.actualizarPerfil(updatePerfilDTO, authentication);

        verify(usuarioService).actualizarPerfil("123", updatePerfilDTO);
    }
}
