package com.barberia.modules.modulo_citas.repositories;

import com.barberia.modules.modulo_citas.models.entities.BitácoraAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para BitácoraAuditoria
 */
@Repository
public interface BitácoraAuditoriaRepository extends JpaRepository<BitácoraAuditoria, Long> {

    List<BitácoraAuditoria> findByIdCita(Long idCita);

    List<BitácoraAuditoria> findByIdCitaOrderByTimestampEventoDesc(Long idCita);

    List<BitácoraAuditoria> findByIdUsuarioActor(String idUsuarioActor);

    List<BitácoraAuditoria> findByAccion(String accion);

    List<BitácoraAuditoria> findByTimestampEventoBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT a FROM BitácoraAuditoria a WHERE a.idCita = ?1 AND a.timestampEvento >= ?2 ORDER BY a.timestampEvento DESC")
    List<BitácoraAuditoria> findRecentAudits(Long idCita, LocalDateTime desde);
}
