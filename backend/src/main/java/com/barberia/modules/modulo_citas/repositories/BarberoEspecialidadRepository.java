package com.barberia.modules.modulo_citas.repositories;

import com.barberia.modules.modulo_citas.models.entities.BarberoEspecialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para BarberoEspecialidad
 */
@Repository
public interface BarberoEspecialidadRepository extends JpaRepository<BarberoEspecialidad, Long> {

    Optional<BarberoEspecialidad> findByIdUsuarioBarberoAndIdServicio(String idUsuarioBarbero, Long idServicio);

    List<BarberoEspecialidad> findByIdUsuarioBarbero(String idUsuarioBarbero);

    List<BarberoEspecialidad> findByIdServicio(Long idServicio);

    boolean existsByIdUsuarioBarberoAndIdServicio(String idUsuarioBarbero, Long idServicio);
}
