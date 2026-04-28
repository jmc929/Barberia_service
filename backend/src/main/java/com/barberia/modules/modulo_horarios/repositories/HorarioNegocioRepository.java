package com.barberia.modules.modulo_horarios.repositories;

import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioNegocioRepository extends JpaRepository<HorarioNegocio, Long> {
    List<HorarioNegocio> findAllByOrderByIdDiaAsc();
    Optional<HorarioNegocio> findByIdDia(Long idDia);
}
