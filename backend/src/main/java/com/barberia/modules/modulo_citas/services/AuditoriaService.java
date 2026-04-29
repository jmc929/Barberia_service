package com.barberia.modules.modulo_citas.services;

import com.barberia.modules.modulo_citas.models.entities.BitácoraAuditoria;
import com.barberia.modules.modulo_citas.repositories.BitácoraAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Servicio para registrar auditoría de cambios en citas
 * Permite trazabilidad completa de quién hizo qué y cuándo
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final BitácoraAuditoriaRepository bitácoraRepository;

    /**
     * Registra un cambio en la auditoría
     *
     * @param idCita ID de la cita afectada
     * @param idUsuarioActor ID del usuario que realizó la acción
     * @param accion Tipo de acción (CREATE, UPDATE, CANCEL)
     * @param estadoAnterior Estado anterior (null si CREATE)
     * @param estadoNuevo Estado nuevo
     * @param detalles Detalles adicionales en formato JSONB (opcional)
     */
    @Transactional
    public void registrar(Long idCita, String idUsuarioActor, String accion,
                         String estadoAnterior, String estadoNuevo, Map<String, Object> detalles) {
        BitácoraAuditoria auditoria = new BitácoraAuditoria();
        auditoria.setIdCita(idCita);
        auditoria.setIdUsuarioActor(idUsuarioActor);
        auditoria.setAccion(accion);
        auditoria.setEstadoAnterior(estadoAnterior);
        auditoria.setEstadoNuevo(estadoNuevo);
        auditoria.setDetallesCambio(detalles);
        auditoria.setTimestampEvento(LocalDateTime.now());

        bitácoraRepository.save(auditoria);
    }

    /**
     * Registra sin detalles adicionales
     */
    @Transactional
    public void registrar(Long idCita, String idUsuarioActor, String accion,
                         String estadoAnterior, String estadoNuevo) {
        registrar(idCita, idUsuarioActor, accion, estadoAnterior, estadoNuevo, null);
    }

    /**
     * Obtiene el historial de auditoría de una cita
     */
    @Transactional(readOnly = true)
    public java.util.List<BitácoraAuditoria> obtenerHistorial(Long idCita) {
        return bitácoraRepository.findByIdCitaOrderByTimestampEventoDesc(idCita);
    }
}
