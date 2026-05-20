package com.barberia.modules.modulo_citas.services;

import com.barberia.modules.modulo_citas.dto.CompletarCitaRequestDTO;
import com.barberia.modules.modulo_citas.dto.CompletarCitaResponseDTO;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Map;

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
        String usuarioActual = obtenerNumeroDocumento(authentication);
        // Validar que el usuario autenticado es el peluquero asignado
        if (!cita.getNumeroDocumentoPeluquero().equals(usuarioActual)) {
            throw new SecurityException("No tienes permiso para completar esta cita");
        }

        String estadoAnterior = cita.getIdEstado() == null ? "DESCONOCIDO" : String.valueOf(cita.getIdEstado());

        // Marcar la cita como completada usando el estado 6
        cita.setIdEstado(6L);
        citaRepository.save(cita);
        return CompletarCitaResponseDTO.builder()
                .idCita(cita.getNoCita())
                .estadoAnterior(estadoAnterior)
            .estadoActual("COMPLETADA")
                .mensaje("Cita completada exitosamente")
                .build();
    }

    @SuppressWarnings("unchecked")
    private String obtenerNumeroDocumento(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> detailsMap) {
            Object numeroDocumento = detailsMap.get("numeroDocumento");
            if (numeroDocumento != null) {
                return numeroDocumento.toString();
            }
        }
        return authentication.getName();
    }
}
