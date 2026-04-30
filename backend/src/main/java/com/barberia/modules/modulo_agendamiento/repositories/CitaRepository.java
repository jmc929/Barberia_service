package com.barberia.modules.modulo_agendamiento.repositories;

import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository("citaAgendamientoRepository")
public interface CitaRepository extends JpaRepository<Cita, Long> {

    @Query("SELECT c FROM CitaAgendamiento c WHERE c.numeroDocumentoPeluquero = :numeroDocumentoPeluquero " +
           "AND c.fechaCita = :fechaCita AND c.idEstado = :estadoActivo " +
           "AND c.horaInicioCita < :fin AND c.horaFinCita > :inicio")
    List<Cita> findConflicts(@Param("numeroDocumentoPeluquero") String numeroDocumentoPeluquero,
                             @Param("fechaCita") LocalDate fechaCita,
                             @Param("estadoActivo") long estadoActivo,
                             @Param("inicio") LocalTime inicio,
                             @Param("fin") LocalTime fin);

    @Query("SELECT c FROM CitaAgendamiento c WHERE c.numeroDocumentoPeluquero = :numeroDocumentoPeluquero " +
           "AND c.fechaCita = :fechaCita AND c.idEstado = :estadoActivo " +
           "AND c.horaInicioCita < :fin AND c.horaFinCita > :inicio")
    List<Cita> findConflictsForUpdate(@Param("numeroDocumentoPeluquero") String numeroDocumentoPeluquero,
                                      @Param("fechaCita") LocalDate fechaCita,
                                      @Param("estadoActivo") long estadoActivo,
                                      @Param("inicio") LocalTime inicio,
                                      @Param("fin") LocalTime fin);

    @Query("SELECT c FROM CitaAgendamiento c WHERE c.numeroDocumentoPeluquero = :numeroDocumentoPeluquero " +
           "AND c.noCita != :noCita AND c.fechaCita = :fechaCita AND c.idEstado = :estadoActivo " +
           "AND c.horaInicioCita < :fin AND c.horaFinCita > :inicio")
    List<Cita> findConflictsForUpdateExcludingNoCita(@Param("numeroDocumentoPeluquero") String numeroDocumentoPeluquero,
                                                    @Param("noCita") Long noCita,
                                                    @Param("fechaCita") LocalDate fechaCita,
                                                    @Param("estadoActivo") long estadoActivo,
                                                    @Param("inicio") LocalTime inicio,
                                                    @Param("fin") LocalTime fin);

    Optional<Cita> findById(Long noCita);
}
