package com.barberia.modules.modulo_usuarios.services;

import com.barberia.modules.modulo_usuarios.models.dtos.RegistroDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UpdatePerfilDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioCliente;
    private Usuario usuarioBarbero;
    private RegistroDTO registroDTO;
    private UpdatePerfilDTO updatePerfilDTO;

    @BeforeEach
    void setUp() {
        usuarioCliente = Usuario.builder()
                .numeroDocumento("123")
                .numeroCelular("+573001234567")
                .email("cliente@test.com")
                .nombrePersona("Cliente Test")
                .contrasenaHasheada("hashedpass")
                .idRol(3)
                .idEstado(1)
                .build();

        usuarioBarbero = Usuario.builder()
                .numeroDocumento("456")
                .numeroCelular("+573009876543")
                .email("barbero@test.com")
                .nombrePersona("Barbero Test")
                .contrasenaHasheada("hashedpass2")
                .idRol(2)
                .idEstado(1)
                .build();

        registroDTO = RegistroDTO.builder()
                .numeroDocumento("789")
                .numeroCelular("+573001112233")
                .email("nuevo@test.com")
                .nombrePersona("Nuevo Usuario")
                .contraseña("password123")
                .confirmarContraseña("password123")
                .build();

        updatePerfilDTO = UpdatePerfilDTO.builder()
                .numeroCelular("+573004445566")
                .email("nuevoemail@test.com")
                .nombrePersona("Nombre Actualizado")
                .build();
    }

    @Test
    void registrarPersona_exitoso() {
        when(usuarioRepository.existsByNumeroDocumento("789")).thenReturn(false);
        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedpass");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioDTO result = usuarioService.registrarPersona(registroDTO);

        assertNotNull(result);
        assertEquals("789", result.getNumeroDocumento());
        assertEquals("nuevo@test.com", result.getEmail());
    }

    @Test
    void registrarPersona_cuandoDocumentoYaExiste_lanzaExcepcion() {
        when(usuarioRepository.existsByNumeroDocumento("789")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrarPersona(registroDTO));
    }

    @Test
    void registrarPersona_cuandoEmailYaExiste_lanzaExcepcion() {
        when(usuarioRepository.existsByNumeroDocumento("789")).thenReturn(false);
        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrarPersona(registroDTO));
    }

    @Test
    void registrarPersona_cuandoContrasenasNoCoinciden_lanzaExcepcion() {
        registroDTO.setConfirmarContraseña("diferente");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrarPersona(registroDTO));
    }

    @Test
    void registrarPersona_cuandoContrasenaMuyCorta_lanzaExcepcion() {
        registroDTO.setContraseña("12345");
        registroDTO.setConfirmarContraseña("12345");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrarPersona(registroDTO));
    }

    @Test
    void obtenerPorNumeroDocumento_exitoso() {
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));

        UsuarioDTO result = usuarioService.obtenerPorNumeroDocumento("123");

        assertNotNull(result);
        assertEquals("123", result.getNumeroDocumento());
    }

    @Test
    void obtenerPorNumeroDocumento_cuandoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.obtenerPorNumeroDocumento("999"));
    }

    @Test
    void obtenerPorEmail_exitoso() {
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(usuarioCliente));

        UsuarioDTO result = usuarioService.obtenerPorEmail("cliente@test.com");

        assertNotNull(result);
        assertEquals("cliente@test.com", result.getEmail());
    }

    @Test
    void obtenerPorEmail_cuandoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.obtenerPorEmail("noexiste@test.com"));
    }

    @Test
    void obtenerTodas_retornaLista() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioCliente, usuarioBarbero));

        List<UsuarioDTO> result = usuarioService.obtenerTodas();

        assertEquals(2, result.size());
    }

    @Test
    void obtenerBarberos_retornaSoloBarberos() {
        Usuario barbero2 = Usuario.builder()
                .numeroDocumento("789")
                .numeroCelular("+573001112233")
                .email("barbero2@test.com")
                .nombrePersona("Barbero 2")
                .idRol(2)
                .idEstado(1)
                .build();
        when(usuarioRepository.findByIdRol(2)).thenReturn(List.of(usuarioBarbero, barbero2));

        List<UsuarioDTO> result = usuarioService.obtenerBarberos();

        assertEquals(2, result.size());
    }

    @Test
    void cambiarRol_exitoso() {
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioCliente);

        UsuarioDTO result = usuarioService.cambiarRol("123", 2);

        assertNotNull(result);
        assertEquals(2, usuarioCliente.getIdRol().intValue());
    }

    @Test
    void cambiarRol_cuandoUsuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.cambiarRol("999", 2));
    }

    @Test
    void bloquearUsuario_exitoso() {
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioCliente);

        UsuarioDTO result = usuarioService.bloquearUsuario("123");

        assertNotNull(result);
        assertEquals(4, usuarioCliente.getIdEstado().intValue());
    }

    @Test
    void bloquearUsuario_cuandoNoEsCliente_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("456")).thenReturn(Optional.of(usuarioBarbero));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.bloquearUsuario("456"));
    }

    @Test
    void bloquearUsuario_cuandoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.bloquearUsuario("999"));
    }

    @Test
    void obtenerUsuariosBloqueados_filtraSoloClientes() {
        Usuario clienteBloqueado = Usuario.builder()
                .numeroDocumento("111")
                .nombrePersona("Cliente Bloqueado")
                .idRol(3)
                .idEstado(4)
                .build();
        Usuario barberoBloqueado = Usuario.builder()
                .numeroDocumento("222")
                .nombrePersona("Barbero Bloqueado")
                .idRol(2)
                .idEstado(4)
                .build();
        when(usuarioRepository.findByIdEstado(4)).thenReturn(List.of(clienteBloqueado, barberoBloqueado));

        List<UsuarioDTO> result = usuarioService.obtenerUsuariosBloqueados();

        assertEquals(1, result.size());
        assertEquals("111", result.get(0).getNumeroDocumento());
    }

    @Test
    void desbloquearUsuario_exitoso() {
        usuarioCliente.setIdEstado(4);
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioCliente);

        UsuarioDTO result = usuarioService.desbloquearUsuario("123");

        assertNotNull(result);
        assertEquals(1, usuarioCliente.getIdEstado().intValue());
    }

    @Test
    void desbloquearUsuario_cuandoNoEstaBloqueado_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.desbloquearUsuario("123"));
    }

    @Test
    void desbloquearUsuario_cuandoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.desbloquearUsuario("999"));
    }

    @Test
    void actualizarPerfil_exitoso() {
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioCliente);

        UsuarioDTO result = usuarioService.actualizarPerfil("123", updatePerfilDTO);

        assertNotNull(result);
        assertEquals("+573004445566", usuarioCliente.getNumeroCelular());
        assertEquals("nuevoemail@test.com", usuarioCliente.getEmail());
        assertEquals("Nombre Actualizado", usuarioCliente.getNombrePersona());
    }

    @Test
    void actualizarPerfil_cuandoRequestNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizarPerfil("123", null));
    }

    @Test
    void actualizarPerfil_cuandoUsuarioNoAutenticado_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizarPerfil(null, updatePerfilDTO));
    }

    @Test
    void actualizarPerfil_cuandoUsuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.actualizarPerfil("999", updatePerfilDTO));
    }

    @Test
    void actualizarPerfil_cuandoNumeroCelularInvalido_lanzaExcepcion() {
        updatePerfilDTO.setNumeroCelular("invalido");
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizarPerfil("123", updatePerfilDTO));
    }

    @Test
    void actualizarPerfil_cuandoEmailYaEnUso_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));
        when(usuarioRepository.existsByEmail("nuevoemail@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizarPerfil("123", updatePerfilDTO));
    }

    @Test
    void actualizarPerfil_conCambioContrasenaExitoso() {
        UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                .nombrePersona("Actualizado")
                .currentPassword("oldpass")
                .newPassword("newpass123")
                .confirmarNuevaPassword("newpass123")
                .build();
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioCliente);
        when(passwordEncoder.matches("oldpass", "hashedpass")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("newencoded");

        UsuarioDTO result = usuarioService.actualizarPerfil("123", dto);

        assertNotNull(result);
        assertEquals("newencoded", usuarioCliente.getContrasenaHasheada());
    }

    @Test
    void actualizarPerfil_conCambioContrasenaSinActual_lanzaExcepcion() {
        UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                .newPassword("newpass123")
                .confirmarNuevaPassword("newpass123")
                .build();
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizarPerfil("123", dto));
    }

    @Test
    void actualizarPerfil_conCambioContrasenaIncorrecta_lanzaExcepcion() {
        UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                .currentPassword("wrongpass")
                .newPassword("newpass123")
                .confirmarNuevaPassword("newpass123")
                .build();
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));
        when(passwordEncoder.matches("wrongpass", "hashedpass")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizarPerfil("123", dto));
    }

    @Test
    void actualizarPerfil_conCambioContrasenaCorta_lanzaExcepcion() {
        UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                .currentPassword("oldpass")
                .newPassword("12345")
                .confirmarNuevaPassword("12345")
                .build();
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));
        when(passwordEncoder.matches("oldpass", "hashedpass")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizarPerfil("123", dto));
    }

    @Test
    void actualizarPerfil_conCambioContrasenaNoCoinciden_lanzaExcepcion() {
        UpdatePerfilDTO dto = UpdatePerfilDTO.builder()
                .currentPassword("oldpass")
                .newPassword("newpass123")
                .confirmarNuevaPassword("different")
                .build();
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));
        when(passwordEncoder.matches("oldpass", "hashedpass")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizarPerfil("123", dto));
    }

    @Test
    void actualizarPerfil_cuandoNoHayCampos_lanzaExcepcion() {
        UpdatePerfilDTO dtoVacio = new UpdatePerfilDTO();
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.actualizarPerfil("123", dtoVacio));
    }

    @Test
    void deshabilitarBarbero_exitoso() {
        when(usuarioRepository.findByNumeroDocumento("456")).thenReturn(Optional.of(usuarioBarbero));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioBarbero);

        UsuarioDTO result = usuarioService.deshabilitarBarbero("456");

        assertNotNull(result);
        assertEquals(5, usuarioBarbero.getIdEstado().intValue());
    }

    @Test
    void deshabilitarBarbero_cuandoNoEsBarbero_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.deshabilitarBarbero("123"));
    }

    @Test
    void deshabilitarBarbero_cuandoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.deshabilitarBarbero("999"));
    }

    @Test
    void habilitarBarbero_exitoso() {
        usuarioBarbero.setIdEstado(5);
        when(usuarioRepository.findByNumeroDocumento("456")).thenReturn(Optional.of(usuarioBarbero));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioBarbero);

        UsuarioDTO result = usuarioService.habilitarBarbero("456");

        assertNotNull(result);
        assertEquals(1, usuarioBarbero.getIdEstado().intValue());
    }

    @Test
    void habilitarBarbero_cuandoNoEsBarbero_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(usuarioCliente));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.habilitarBarbero("123"));
    }

    @Test
    void habilitarBarbero_cuandoNoEstaDeshabilitado_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("456")).thenReturn(Optional.of(usuarioBarbero));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.habilitarBarbero("456"));
    }

    @Test
    void habilitarBarbero_cuandoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.habilitarBarbero("999"));
    }
}
