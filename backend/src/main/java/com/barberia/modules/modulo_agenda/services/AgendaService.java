package com.barberia.modules.modulo_agenda.services;

import com.barberia.modules.modulo_agenda.models.dtos.AgendaResponseDTO;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_servicios.repositories.ServicioRepository;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendaService {
    private static final Long ESTADO_ACTIVO = 1L;

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    /**
     * Obtiene la agenda de un peluquero por su documento.
     */
    public List<AgendaResponseDTO> obtenerAgendaPeluquero(String numeroDocumentoPeluquero) {
        List<Cita> citas = citaRepository
                .findByNumeroDocumentoPeluqueroAndIdEstadoOrderByFechaCitaAscHoraInicioCitaAsc(numeroDocumentoPeluquero, ESTADO_ACTIVO);
        return citas.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private AgendaResponseDTO mapToDTO(Cita cita) {
        // Obtener nombre del cliente
        Usuario cliente = usuarioRepository.findByNumeroDocumento(cita.getNumeroDocumentoCliente()).orElse(null);
        String nombreCliente = cliente != null ? cliente.getNombrePersona() : "Desconocido";

        Servicio servicio = servicioRepository.findById(cita.getIdServicio()).orElse(null);
        String nombreServicio = servicio != null ? servicio.getNombreServicio() : "Desconocido";

        return AgendaResponseDTO.builder()
                .fecha(cita.getFechaCita())
                .horaInicio(cita.getHoraInicioCita())
                .horaFin(cita.getHoraFinCita())
                .nombreCliente(nombreCliente)
                .idServicio(cita.getIdServicio())
                .nombreServicio(nombreServicio)
                .estado(obtenerNombreEstado(cita.getIdEstado()))
                .build();
    }

    private String obtenerNombreEstado(Long idEstado) {
        if (idEstado == null) {
            return "DESCONOCIDO";
        }

        if (idEstado.equals(1L)) {
            return EstadoCita.ACTIVO.name();
        }
        if (idEstado.equals(3L)) {
            return EstadoCita.CANCELADA.name();
        }
        if (idEstado.equals(6L)) {
            return EstadoCita.COMPLETADA.name();
        }

        return String.valueOf(idEstado);
    }
}
