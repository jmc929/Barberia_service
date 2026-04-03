package com.barberia.modules.modulo_usuarios.services;

import com.barberia.modules.modulo_usuarios.models.dtos.RegistroDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registra una nueva persona con contraseña
     * 
     * @param registroDTO datos del registro (documento, celular, email, nombre, contraseña)
     * @return UsuarioDTO de la persona creada
     * @throws IllegalArgumentException si hay validación fallida
     */
    public UsuarioDTO registrarPersona(RegistroDTO registroDTO) {
        // Validación: numero_documento debe ser único
        if (usuarioRepository.existsByNumeroDocumento(registroDTO.getNumeroDocumento())) {
            throw new IllegalArgumentException("Error: El número de documento ya está registrado");
        }

        // Validación: email debe ser único
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new IllegalArgumentException("Error: El email ya está registrado");
        }

        // Validación: contraseñas coinciden
        if (!registroDTO.getContraseña().equals(registroDTO.getConfirmarContraseña())) {
            throw new IllegalArgumentException("Error: Las contraseñas no coinciden");
        }

        // Validación: contraseña mínima
        if (registroDTO.getContraseña().length() < 6) {
            throw new IllegalArgumentException("Error: La contraseña debe tener al menos 6 caracteres");
        }

        // Crear usuario con contraseña hasheada
        Usuario usuario = Usuario.builder()
            .numeroDocumento(registroDTO.getNumeroDocumento())
            .numeroCelular(registroDTO.getNumeroCelular())
            .email(registroDTO.getEmail())
            .nombrePersona(registroDTO.getNombrePersona())
            .contrasenaHasheada(passwordEncoder.encode(registroDTO.getContraseña()))
            .idRol(3)           // Siempre 3
            .idEstado(1)        // Siempre 1 (activo)
            .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertirADTO(usuarioGuardado);
    }

    /**
     * Obtiene una persona por su número de documento
     */
    public UsuarioDTO obtenerPorNumeroDocumento(String numeroDocumento) {
        Usuario usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con documento: " + numeroDocumento));
        return convertirADTO(usuario);
    }

    /**
     * Obtiene una persona por su email
     */
    public UsuarioDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con email: " + email));
        return convertirADTO(usuario);
    }

    /**
     * Obtiene todas las personas
     */
    public List<UsuarioDTO> obtenerTodas() {
        return usuarioRepository.findAll()
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    /**
     * Convierte una Entity Usuario a DTO (sin exponer contraseña hasheada)
     */
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
