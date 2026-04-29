package com.barberia.modules.modulo_citas.repositories;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Cita con soporte para Pessimistic Locking
 * CRÍTICO: Métodos con @Lock previenen race conditions
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca citas confirmadas de un barbero en un rango de tiempo
     * Usado para detectar solapamientos
     * Con PESSIMISTIC_WRITE lock para evitar race conditions
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cita c WHERE c.idBarbero = ?1 AND c.estadoCita = ?2 " +
           "AND c.fechaHora < ?3 AND DATE_ADD(c.fechaHora, INTERVAL c.duracionMinutos MINUTE) > ?4")
    Optional<Cita> findConflictingCita(String idBarbero, EstadoCita estado, LocalDateTime endTime, LocalDateTime startTime);

    /**
     * Busca todas las citas de un cliente confirmadas
     */
    Page<Cita> findByIdClienteAndEstadoCita(String idCliente, EstadoCita estado, Pageable pageable);

    /**
     * Busca todas las citas de un cliente
     */
    Page<Cita> findByIdCliente(String idCliente, Pageable pageable);

    /**
     * Busca todas las citas de un barbero
     */
    Page<Cita> findByIdBarbero(String idBarbero, Pageable pageable);

    /**
     * Busca citas confirmadas de un barbero en un rango de fechas
     */
    List<Cita> findByIdBarberoAndEstadoCitaAndFechaHoraBetween(
        String idBarbero,
        EstadoCita estado,
        LocalDateTime inicio,
        LocalDateTime fin
    );

    /**
     * Busca una cita específica con lock para actualización segura
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cita c WHERE c.idCita = ?1")
    Optional<Cita> findByIdCitaWithLock(Long idCita);

    /**
     * Busca citas confirmadas de un barbero en una fecha específica
     */
    List<Cita> findByIdBarberoAndFechaHoraBetweenAndEstadoCita(
        String idBarbero,
        LocalDateTime inicio,
        LocalDateTime fin,
        EstadoCita estado
    );

    /**
     * Cuenta citas confirmadas del barbero en un rango de tiempo
     */
    long countByIdBarberoAndEstadoCitaAndFechaHoraBetween(
        String idBarbero,
        EstadoCita estado,
        LocalDateTime inicio,
        LocalDateTime fin
    );
}
