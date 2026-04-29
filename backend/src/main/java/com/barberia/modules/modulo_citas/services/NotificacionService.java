package com.barberia.modules.modulo_citas.services;

import com.barberia.modules.modulo_citas.models.entities.CitaNotificacion;
import com.barberia.modules.modulo_citas.models.dtos.NotificacionDTO;
import com.barberia.modules.modulo_citas.enums.TipoNotificacion;
import com.barberia.modules.modulo_citas.repositories.CitaNotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para gestionar notificaciones de citas
 * Crea, marca como leídas y lista notificaciones para usuarios
 */
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final CitaNotificacionRepository notificacionRepository;

    /**
     * Crea una nueva notificación
     *
     * @param idCita ID de la cita relacionada
     * @param idUsuarioDestinatario Usuario que recibe la notificación
     * @param tipo Tipo de notificación
     * @param mensaje Contenido de la notificación
     */
    @Transactional
    public NotificacionDTO crearNotificacion(Long idCita, String idUsuarioDestinatario,
                                              TipoNotificacion tipo, String mensaje) {
        CitaNotificacion notificacion = new CitaNotificacion();
        notificacion.setIdCita(idCita);
        notificacion.setIdUsuarioDestinatario(idUsuarioDestinatario);
        notificacion.setTipoNotificacion(tipo);
        notificacion.setMensaje(mensaje);
        notificacion.setFechaEnvio(LocalDateTime.now());
        notificacion.setLeida(false);

        CitaNotificacion guardada = notificacionRepository.save(notificacion);
        return mapToDTO(guardada);
    }

    /**
     * Obtiene las notificaciones de un usuario
     */
    @Transactional(readOnly = true)
    public Page<NotificacionDTO> obtenerMisNotificaciones(String idUsuario, Pageable pageable) {
        return notificacionRepository.findByIdUsuarioDestinatario(idUsuario, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Obtiene solo notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public Page<NotificacionDTO> obtenerNotificacionesNoLeidas(String idUsuario, Pageable pageable) {
        return notificacionRepository.findByIdUsuarioDestinatarioAndLeida(idUsuario, false, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Marca una notificación como leída
     */
    @Transactional
    public NotificacionDTO marcarComoLeida(Long idNotificacion) {
        CitaNotificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new com.barberia.shared.exceptions.ResourceNotFoundException(
                    "Notificación no encontrada"));

        notificacion.setLeida(true);
        CitaNotificacion actualizada = notificacionRepository.save(notificacion);
        return mapToDTO(actualizada);
    }

    /**
     * Obtiene el conteo de notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public long contarNotificacionesNoLeidas(String idUsuario) {
        return notificacionRepository.countByIdUsuarioDestinatarioAndLeida(idUsuario, false);
    }

    /**
     * Convierte entidad a DTO
     */
    private NotificacionDTO mapToDTO(CitaNotificacion notificacion) {
        return NotificacionDTO.builder()
                .idNotificacion(notificacion.getIdNotificacion())
                .idCita(notificacion.getIdCita())
                .tipo(notificacion.getTipoNotificacion().name())
                .mensaje(notificacion.getMensaje())
                .fechaEnvio(notificacion.getFechaEnvio())
                .leida(notificacion.getLeida())
                .build();
    }
}
