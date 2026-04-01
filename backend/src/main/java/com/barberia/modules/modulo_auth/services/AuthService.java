package com.barberia.modules.modulo_auth.services;

import com.barberia.modules.modulo_auth.models.dtos.LoginDTO;
import com.barberia.modules.modulo_auth.models.dtos.LoginResponseDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Autentica un usuario con email y contraseña
     * Devuelve JWT token si la autenticación es exitosa
     * 
     * @param loginDTO contiene email y contraseña
     * @return LoginResponseDTO con token y datos del usuario
     * @throws IllegalArgumentException si credenciales son inválidas
     */
    public LoginResponseDTO iniciarSesion(LoginDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Error: Credenciales inválidas"));

        // Validar contraseña
        if (!passwordEncoder.matches(loginDTO.getContraseña(), usuario.getContrasenaHasheada())) {
            throw new IllegalArgumentException("Error: Credenciales inválidas");
        }

        // Generar JWT token
        String token = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getId());

        return LoginResponseDTO.builder()
            .token(token)
            .type("Bearer")
            .usuario(convertirADTO(usuario))
            .build();
    }

    /**
     * Convierte Entity Usuario a DTO (sin exponer contraseña hasheada)
     */
    private UsuarioDTO convertirADTO(Usuario usuario) {
        return UsuarioDTO.builder()
            .id(usuario.getId())
            .numeroDocumento(usuario.getNumeroDocumento())
            .numeroCelular(usuario.getNumeroCelular())
            .email(usuario.getEmail())
            .nombrePersona(usuario.getNombrePersona())
            .idEstado(usuario.getIdEstado())
            .idRol(usuario.getIdRol())
            .fechaRegistro(usuario.getFechaRegistro())
            .ultimoAcceso(usuario.getUltimoAcceso())
            .build();
    }
}
