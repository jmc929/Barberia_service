package com.barberia.modules.modulo_auth.services;

import com.barberia.modules.modulo_auth.models.dtos.LoginDTO;
import com.barberia.modules.modulo_auth.models.dtos.LoginResponseDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponseDTO iniciarSesion(LoginDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Error: Credenciales inválidas"));

        // Validar contraseña
        if (!passwordEncoder.matches(loginDTO.getContraseña(), usuario.getContrasenaHasheada())) {
            throw new IllegalArgumentException("Error: Credenciales inválidas");
        }

        // Generar JWT token
        String token = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getNumeroDocumento(), usuario.getIdRol());

        return LoginResponseDTO.builder()
            .token(token)
            .type("Bearer")
            .usuario(convertirADTO(usuario))
            .build();
    }

    private UsuarioDTO convertirADTO(Usuario usuario) {
        return UsuarioDTO.builder()
            .numeroDocumento(usuario.getNumeroDocumento())
            .numeroCelular(usuario.getNumeroCelular())
            .email(usuario.getEmail())
            .nombrePersona(usuario.getNombrePersona())
            .idEstado(usuario.getIdEstado())
            .idRol(usuario.getIdRol())
            .fechaRegistro(usuario.getFechaRegistro())
            .build();
    }
}
