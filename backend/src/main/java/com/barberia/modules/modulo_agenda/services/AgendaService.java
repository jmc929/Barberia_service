package com.barberia.modules.modulo_agenda.services;

import com.barberia.modules.modulo_agenda.models.dtos.AgendaResponseDTO;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendaService {
    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene la agenda de un peluquero por su documento.
     */
    public List<AgendaResponseDTO> obtenerAgendaPeluquero(String numeroDocumentoPeluquero) {
        List<Cita> citas = citaRepository.findAll().stream()
                .filter(c -> c.getNumeroDocumentoPeluquero().equals(numeroDocumentoPeluquero))
                .sorted((c1, c2) -> {
                    int cmp = c1.getFechaCita().compareTo(c2.getFechaCita());
                    if (cmp == 0) {
                        return c1.getHoraInicioCita().compareTo(c2.getHoraInicioCita());
                    }
                    return cmp;
                })
                .collect(Collectors.toList());
        return citas.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Obtiene la agenda de todos los peluqueros.
     */
    public List<AgendaResponseDTO> obtenerAgendaCompleta() {
        List<Cita> citas = citaRepository.findAll();
        return citas.stream()
                .sorted((c1, c2) -> {
                    int cmp = c1.getFechaCita().compareTo(c2.getFechaCita());
                    if (cmp == 0) {
                        return c1.getHoraInicioCita().compareTo(c2.getHoraInicioCita());
                    }
                    return cmp;
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AgendaResponseDTO mapToDTO(Cita cita) {
        // Obtener nombre del cliente
        Usuario cliente = usuarioRepository.findByNumeroDocumento(cita.getNumeroDocumentoCliente()).orElse(null);
        String nombreCliente = cliente != null ? cliente.getNombrePersona() : "Desconocido";
        // Obtener nombre del servicio (puedes mejorar esto si tienes un repositorio de servicios)
        String nombreServicio = "Servicio " + cita.getIdServicio();
        return AgendaResponseDTO.builder()
                .fecha(cita.getFechaCita())
                .horaInicio(cita.getHoraInicioCita())
                .horaFin(cita.getHoraFinCita())
                .nombreCliente(nombreCliente)
                .nombreServicio(nombreServicio)
                .build();
    }
}
