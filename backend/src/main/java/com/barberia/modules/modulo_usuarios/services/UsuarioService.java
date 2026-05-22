package com.barberia.modules.modulo_usuarios.services;

import com.barberia.modules.modulo_usuarios.models.dtos.RegistroDTO;
import com.barberia.modules.modulo_usuarios.models.dtos.UsuarioDTO;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
     * Obtiene todos los barberos (idRol = 2)
     */
    public List<UsuarioDTO> obtenerBarberos() {
        return usuarioRepository.findByIdRol(2)
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    /**
     * Cambia el rol de un usuario
     */
    public UsuarioDTO cambiarRol(String numeroDocumento, Integer nuevoRol) {
        Usuario usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con documento: " + numeroDocumento));

        usuario.setIdRol(nuevoRol);
        return convertirADTO(usuarioRepository.save(usuario));
    }

    /**
     * Bloquea un cliente (rol 3) cambiando su idEstado a 4
     * 
     * @param numeroDocumento documento del usuario a bloquear
     * @return UsuarioDTO del usuario bloqueado
     * @throws IllegalArgumentException si el usuario no tiene rol 3 (cliente)
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public UsuarioDTO bloquearUsuario(String numeroDocumento) {
        Usuario usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con documento: " + numeroDocumento));

        // Validar que sea cliente (rol 3)
        if (!usuario.getIdRol().equals(3)) {
            throw new IllegalArgumentException("Error: Solo se pueden bloquear clientes (rol 3). Este usuario tiene rol " + usuario.getIdRol());
        }

        usuario.setIdEstado(4); // 4 = Bloqueado
        return convertirADTO(usuarioRepository.save(usuario));
    }

    /**
     * Obtiene todos los clientes bloqueados (idEstado = 4 y idRol = 3)
     * 
     * @return Lista de UsuarioDTO de clientes bloqueados
     */
    public List<UsuarioDTO> obtenerUsuariosBloqueados() {
        return usuarioRepository.findByIdEstado(4)
            .stream()
            .filter(usuario -> usuario.getIdRol().equals(3)) // Solo clientes
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    /**
     * Desbloquea un cliente (rol 3) cambiando su idEstado de 4 a 1
     * 
     * @param numeroDocumento documento del usuario a desbloquear
     * @return UsuarioDTO del usuario desbloqueado
     * @throws IllegalArgumentException si el usuario no está bloqueado
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public UsuarioDTO desbloquearUsuario(String numeroDocumento) {
        Usuario usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con documento: " + numeroDocumento));

        // Validar que esté bloqueado (idEstado = 4)
        if (!usuario.getIdEstado().equals(4)) {
            throw new IllegalArgumentException("Error: El usuario no está bloqueado. Estado actual: " + usuario.getIdEstado());
        }

        usuario.setIdEstado(1); // 1 = Activo
        return convertirADTO(usuarioRepository.save(usuario));
    }

    /**
     * Actualiza el perfil del usuario identificado por numeroDocumento.
     * Solo permite actualizar el usuario mismo.
     * Valida que los campos no estén vacíos y que el número de celular tenga formato.
     */
    public UsuarioDTO actualizarPerfil(String numeroDocumento, com.barberia.modules.modulo_usuarios.models.dtos.UpdatePerfilDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request es requerido");
        }

        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        Usuario usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con documento: " + numeroDocumento));

        boolean anyUpdated = false;

        // Si se provee numeroCelular -> validar y setear
        if (dto.getNumeroCelular() != null && !dto.getNumeroCelular().isBlank()) {
            String telefono = dto.getNumeroCelular();
            if (!telefono.matches("^\\+?[0-9]{7,20}$")) {
                throw new IllegalArgumentException("Numero de celular inválido");
            }
            usuario.setNumeroCelular(telefono);
            anyUpdated = true;
        }

        // Si se provee email -> validar unico y setear
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String nuevoEmail = dto.getEmail();
            if (!usuario.getEmail().equalsIgnoreCase(nuevoEmail) && usuarioRepository.existsByEmail(nuevoEmail)) {
                throw new IllegalArgumentException("El email ya está en uso por otro usuario");
            }
            usuario.setEmail(nuevoEmail);
            anyUpdated = true;
        }

        // Si se provee nombrePersona -> setear
        if (dto.getNombrePersona() != null && !dto.getNombrePersona().isBlank()) {
            usuario.setNombrePersona(dto.getNombrePersona());
            anyUpdated = true;
        }

        // Manejo de cambio de contraseña (opcional)
        String nueva = dto.getNewPassword();
        if (nueva != null && !nueva.isBlank()) {
            String actual = dto.getCurrentPassword();
            if (actual == null || actual.isBlank()) {
                throw new IllegalArgumentException("Es necesaria la contraseña actual para cambiar la contraseña");
            }

            // Verificar que la contraseña actual coincida
            if (!passwordEncoder.matches(actual, usuario.getContrasenaHasheada())) {
                throw new IllegalArgumentException("Contraseña actual incorrecta");
            }

            if (nueva.length() < 6) {
                throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");
            }

            String confirmar = dto.getConfirmarNuevaPassword();
            if (confirmar == null || !nueva.equals(confirmar)) {
                throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
            }

            usuario.setContrasenaHasheada(passwordEncoder.encode(nueva));
            anyUpdated = true;
        }

        if (!anyUpdated) {
            throw new IllegalArgumentException("No hay campos para actualizar");
        }

        Usuario guardado = usuarioRepository.save(usuario);
        return convertirADTO(guardado);
    }

    /**
     * Deshabilita un barbero (rol 2) cambiando su idEstado a 5
     *
     * @param numeroDocumento documento del usuario a deshabilitar
     * @return UsuarioDTO del barbero deshabilitado
     * @throws IllegalArgumentException si el usuario no es barbero
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public UsuarioDTO deshabilitarBarbero(String numeroDocumento) {
        Usuario usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con documento: " + numeroDocumento));

        if (!usuario.getIdRol().equals(2)) {
            throw new IllegalArgumentException("No es barbero");
        }

        usuario.setIdEstado(5); // 5 = Deshabilitado
        return convertirADTO(usuarioRepository.save(usuario));
    }

    /**
     * Habilita un barbero deshabilitado (rol 2) cambiando su idEstado de 5 a 1
     *
     * @param numeroDocumento documento del usuario a habilitar
     * @return UsuarioDTO del barbero habilitado
     * @throws IllegalArgumentException si el usuario no es barbero o no está deshabilitado
     * @throws ResourceNotFoundException si el usuario no existe
     */
    public UsuarioDTO habilitarBarbero(String numeroDocumento) {
        Usuario usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con documento: " + numeroDocumento));

        if (!usuario.getIdRol().equals(2)) {
            throw new IllegalArgumentException("No es barbero");
        }

        if (!usuario.getIdEstado().equals(5)) {
            throw new IllegalArgumentException("El barbero no está deshabilitado");
        }

        usuario.setIdEstado(1); // 1 = Activo
        return convertirADTO(usuarioRepository.save(usuario));
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
