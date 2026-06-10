package com.barberia.modules.modulo_auth.services;

import com.barberia.modules.modulo_auth.models.dtos.LoginDTO;
import com.barberia.modules.modulo_auth.models.dtos.LoginResponseDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .numeroDocumento("123")
                .email("test@test.com")
                .nombrePersona("Test User")
                .contrasenaHasheada("hashedpass")
                .idRol(3)
                .idEstado(1)
                .build();

        loginDTO = LoginDTO.builder()
                .email("test@test.com")
                .contraseña("password123")
                .build();
    }

    @Test
    void iniciarSesion_exitoso() {
        when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "hashedpass")).thenReturn(true);
        when(jwtTokenProvider.generateToken("test@test.com", "123", 3)).thenReturn("jwt-token");

        LoginResponseDTO result = authService.iniciarSesion(loginDTO);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("Bearer", result.getType());
        assertNotNull(result.getUsuario());
        assertEquals("123", result.getUsuario().getNumeroDocumento());
    }

    @Test
    void iniciarSesion_cuandoCredencialesInvalidas_lanzaExcepcion() {
        when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion(loginDTO));
    }

    @Test
    void iniciarSesion_cuandoContrasenaIncorrecta_lanzaExcepcion() {
        when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "hashedpass")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion(loginDTO));
    }
}
