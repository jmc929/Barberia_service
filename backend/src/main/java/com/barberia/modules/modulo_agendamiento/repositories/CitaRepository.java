package com.barberia.modules.modulo_agendamiento.repositories;

import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository("citaAgendamientoRepository")
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Minimal method signatures used by the service. Implementations may be provided elsewhere.
    List<Cita> findConflicts(String numeroDocumentoPeluquero, LocalDate fechaCita, long estadoActivo, LocalTime inicio, LocalTime fin);

    List<Cita> findConflictsForUpdate(String numeroDocumentoPeluquero, LocalDate fechaCita, long estadoActivo, LocalTime inicio, LocalTime fin);

    List<Cita> findConflictsForUpdateExcludingNoCita(String numeroDocumentoPeluquero, Long noCita, LocalDate fechaCita, long estadoActivo, LocalTime inicio, LocalTime fin);

    Optional<Cita> findById(Long noCita);
}
