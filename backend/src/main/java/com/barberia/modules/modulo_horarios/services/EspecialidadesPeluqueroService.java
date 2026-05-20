package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.dto.ActualizarEspecialidadesPeluqueroDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Servicio para la actualización de especialidades (servicios) del peluquero.
 * No modifica lógica existente, solo actualiza la relación entre peluquero y servicios.
 */
@Service
public class EspecialidadesPeluqueroService {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Actualiza las especialidades del peluquero.
     * @param peluqueroId ID del peluquero
     * @param dto DTO con los IDs de especialidades
     */
    @Transactional
    public void actualizarEspecialidades(Long peluqueroId, ActualizarEspecialidadesPeluqueroDTO dto) {
        // Elimina relaciones actuales
        entityManager.createNativeQuery("DELETE FROM peluquero_servicio WHERE peluquero_id = :peluqueroId")
                .setParameter("peluqueroId", peluqueroId)
                .executeUpdate();
        // Inserta nuevas relaciones
        for (Long especialidadId : dto.getEspecialidadesIds()) {
            entityManager.createNativeQuery("INSERT INTO peluquero_servicio (peluquero_id, servicio_id) VALUES (:peluqueroId, :servicioId)")
                    .setParameter("peluqueroId", peluqueroId)
                    .setParameter("servicioId", especialidadId)
                    .executeUpdate();
        }
    }
}
