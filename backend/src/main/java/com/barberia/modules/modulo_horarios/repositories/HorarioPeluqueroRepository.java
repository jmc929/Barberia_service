package com.barberia.modules.modulo_horarios.repositories;

import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioPeluqueroRepository extends JpaRepository<HorarioPeluquero, Long> {
    List<HorarioPeluquero> findByNumeroDocumentoPeluquero(String numeroDocumentoPeluquero);
    java.util.Optional<HorarioPeluquero> findByNumeroDocumentoPeluqueroAndIdDia(String numeroDocumentoPeluquero, Long idDia);
}
