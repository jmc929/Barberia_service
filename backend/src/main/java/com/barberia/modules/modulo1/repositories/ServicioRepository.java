package com.barberia.modules.modulo1.repositories;

import com.barberia.modules.modulo1.models.entities.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    
    List<Servicio> findByActivoTrue();
    
    Optional<Servicio> findByNombreIgnoreCase(String nombre);
    
    List<Servicio> findByPrecioBetween(Double minPrecio, Double maxPrecio);
}
