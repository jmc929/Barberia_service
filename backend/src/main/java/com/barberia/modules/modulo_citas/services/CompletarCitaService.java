package com.barberia.modules.modulo_citas.services;

import com.barberia.modules.modulo_citas.dto.CompletarCitaRequestDTO;
import com.barberia.modules.modulo_citas.dto.CompletarCitaResponseDTO;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class CompletarCitaService {
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Marca una cita como completada si cumple las reglas de negocio.
     */
    @Transactional
    public CompletarCitaResponseDTO completarCita(CompletarCitaRequestDTO request, Authentication authentication) {
        Optional<Cita> optionalCita = citaRepository.findById(request.getIdCita());
        if (optionalCita.isEmpty()) {
            throw new IllegalArgumentException("Cita no encontrada");
        }
        Cita cita = optionalCita.get();
        String usuarioActual = authentication.getName();
        // Validar que el usuario autenticado es el peluquero asignado
        if (!cita.getNumeroDocumentoPeluquero().equals(usuarioActual)) {
            throw new SecurityException("No tienes permiso para completar esta cita");
        }
        // Validar estado actual
        EstadoCita estadoActual = EstadoCita.values()[cita.getIdEstado().intValue()];
        if (!estadoActual.name().equals("CONFIRMADA")) {
            throw new IllegalStateException("Solo se pueden completar citas en estado CONFIRMADA");
        }
        // Cambiar estado y registrar fecha de completado
        cita.setIdEstado((long) EstadoCita.valueOf("COMPLETADA").ordinal());
        cita.setFechaCompletado(Instant.now());
        citaRepository.save(cita);
        return CompletarCitaResponseDTO.builder()
                .idCita(cita.getNoCita())
                .estadoAnterior("CONFIRMADA")
                .estadoActual("COMPLETADA")
                .fechaCompletado(cita.getFechaCompletado())
                .mensaje("Cita completada exitosamente")
                .build();
    }
}
