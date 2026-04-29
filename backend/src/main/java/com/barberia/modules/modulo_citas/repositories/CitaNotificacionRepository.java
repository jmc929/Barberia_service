package com.barberia.modules.modulo_citas.repositories;

import com.barberia.modules.modulo_citas.models.entities.CitaNotificacion;
import com.barberia.modules.modulo_citas.enums.TipoNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para CitaNotificacion
 */
@Repository
public interface CitaNotificacionRepository extends JpaRepository<CitaNotificacion, Long> {

    Page<CitaNotificacion> findByIdUsuarioDestinatario(String idUsuario, Pageable pageable);

    Page<CitaNotificacion> findByIdUsuarioDestinatarioAndLeida(String idUsuario, Boolean leida, Pageable pageable);

    List<CitaNotificacion> findByIdCita(Long idCita);

    List<CitaNotificacion> findByIdCitaAndTipoNotificacion(Long idCita, TipoNotificacion tipo);

    long countByIdUsuarioDestinatarioAndLeida(String idUsuario, Boolean leida);
}
