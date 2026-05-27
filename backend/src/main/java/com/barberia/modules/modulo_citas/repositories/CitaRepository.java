package com.barberia.modules.modulo_citas.repositories;

import com.barberia.modules.modulo_citas.models.entities.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByIdServicioAndIdEstado(Long idServicio, Long idEstado);
    List<Cita> findByNumeroDocumentoCliente(String numeroDocumentoCliente);
    List<Cita> findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc(String numeroDocumentoPeluquero, Long idEstado);

}

