package com.barberia.modules.modulo_citas.repositories;

import com.barberia.modules.modulo_citas.models.entities.BarberoHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para BarberoHorario
 */
@Repository
public interface BarberoHorarioRepository extends JpaRepository<BarberoHorario, Long> {

    Optional<BarberoHorario> findByIdUsuarioBarberoAndIdDiaSemana(String idUsuarioBarbero, Integer idDiaSemana);

    List<BarberoHorario> findByIdUsuarioBarbero(String idUsuarioBarbero);

    List<BarberoHorario> findByIdDiaSemana(Integer idDiaSemana);

    boolean existsByIdUsuarioBarberoAndIdDiaSemana(String idUsuarioBarbero, Integer idDiaSemana);
}
