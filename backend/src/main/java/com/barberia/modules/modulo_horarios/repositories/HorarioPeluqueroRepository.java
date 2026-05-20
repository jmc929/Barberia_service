package com.barberia.modules.modulo_horarios.repositories;

import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Repositorio para la entidad HorarioPeluquero.
 */
@Repository
public interface HorarioPeluqueroRepository extends JpaRepository<HorarioPeluquero, Long> {
    List<HorarioPeluquero> findByPeluqueroId(Long peluqueroId);
    List<HorarioPeluquero> findByPeluqueroIdAndDiaSemana(Long peluqueroId, DayOfWeek diaSemana);
}
