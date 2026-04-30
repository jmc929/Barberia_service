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
 * Simple Cita repository - keep only JpaRepository methods to avoid derived-query mismatches
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

}
