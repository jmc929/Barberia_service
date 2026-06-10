package com.barberia.modules.modulo_auth.controllers;

import com.barberia.modules.modulo_auth.models.dtos.LoginDTO;
import com.barberia.modules.modulo_auth.models.dtos.LoginResponseDTO;
import com.barberia.modules.modulo_auth.services.AuthService;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.shared.utils.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginDTO loginDTO;
    private LoginResponseDTO loginResponse;

    @BeforeEach
    void setUp() {
        loginDTO = LoginDTO.builder()
                .email("test@test.com")
                .contraseña("password123")
                .build();

        loginResponse = LoginResponseDTO.builder()
                .token("jwt-token")
                .type("Bearer")
                .usuario(UsuarioDTO.builder()
                        .numeroDocumento("123")
                        .email("test@test.com")
                        .build())
                .build();
    }

    @Test
    void login_exitoso() {
        when(authService.iniciarSesion(any())).thenReturn(loginResponse);

        ResponseEntity<ApiResponse<LoginResponseDTO>> result = authController.login(loginDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Login exitoso", result.getBody().getMessage());
        assertNotNull(result.getBody().getData());
    }

    @Test
    void login_cuandoCredencialesInvalidas_retorna401() {
        when(authService.iniciarSesion(any()))
                .thenThrow(new IllegalArgumentException("Credenciales inválidas"));

        ResponseEntity<ApiResponse<LoginResponseDTO>> result = authController.login(loginDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertFalse(result.getBody().isSuccess());
    }

    @Test
    void login_cuandoErrorServidor_retorna500() {
        when(authService.iniciarSesion(any()))
                .thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<ApiResponse<LoginResponseDTO>> result = authController.login(loginDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertFalse(result.getBody().isSuccess());
    }
}
