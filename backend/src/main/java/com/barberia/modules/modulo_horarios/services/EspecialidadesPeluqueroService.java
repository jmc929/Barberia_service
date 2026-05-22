package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.dto.ActualizarEspecialidadesPeluqueroDTO;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Objects;

@Service
public class EspecialidadesPeluqueroService {
    @PersistenceContext
    private EntityManager entityManager;

    @jakarta.annotation.Resource
    private ServicioRepository servicioRepository;

    @Transactional
    public void asociarServicio(String numeroDocumentoPeluquero, Long idServicio) {
        if (numeroDocumentoPeluquero == null || numeroDocumentoPeluquero.isBlank()) {
            throw new IllegalArgumentException("numeroDocumentoPeluquero es requerido");
        }
        Objects.requireNonNull(idServicio, "idServicio es requerido");
        if (!servicioRepository.existsById(idServicio)) {
            throw new IllegalArgumentException("Servicio no encontrado con id: " + idServicio);
        }

        entityManager.createNativeQuery(
                        "INSERT INTO tbl_servicios_por_peluquero (numero_documento_peluquero, id_servicio) " +
                        "SELECT :numeroDocumentoPeluquero, :idServicio " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM tbl_servicios_por_peluquero " +
                        "  WHERE numero_documento_peluquero = :numeroDocumentoPeluquero " +
                        "    AND id_servicio = :idServicio" +
                        ")")
                .setParameter("numeroDocumentoPeluquero", numeroDocumentoPeluquero)
                .setParameter("idServicio", idServicio)
                .executeUpdate();
    }
}
