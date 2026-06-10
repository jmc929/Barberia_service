package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.models.dtos.CitaCreateDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadRequestDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaReprogramarDTO;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class CitaAgendamientoValidator {

    private final HorarioNegocioRepository horarioNegocioRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaAgendamientoValidator(HorarioNegocioRepository horarioNegocioRepository, UsuarioRepository usuarioRepository) {
        this.horarioNegocioRepository = horarioNegocioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public long calcularIdDia(LocalDate fechaCita) {
        return fechaCita.getDayOfWeek().getValue();
    }

    public HorarioNegocioDTO obtenerHorarioParaFecha(LocalDate fechaCita) {
        long idDia = calcularIdDia(fechaCita);
        return horarioNegocioRepository.findByIdDia(idDia)
                .map(horario -> HorarioNegocioDTO.builder()
                        .idHorarioNegocio(horario.getIdHorarioNegocio())
                        .idDia(horario.getIdDia())
                        .horaApertura(horario.getHoraApertura())
                        .horaCierre(horario.getHoraCierre())
                        .localAbierto(horario.getLocalAbierto())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado para el día: " + idDia));
    }

    public void validarHorarioNegocio(LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin) {
        HorarioNegocioDTO horario = obtenerHorarioParaFecha(fechaCita);
        if (horario.getLocalAbierto() == null || !horario.getLocalAbierto()) {
            throw new IllegalArgumentException("El local no está abierto para la fecha seleccionada");
        }
        if (horaInicio.isBefore(horario.getHoraApertura())) {
            throw new IllegalArgumentException("La hora de inicio está fuera del horario de apertura");
        }
        if (horaFin.isAfter(horario.getHoraCierre())) {
            throw new IllegalArgumentException("La cita excede el horario de cierre");
        }
    }

    public void validarSolicitudDisponibilidad(CitaDisponibilidadRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request es requerido");
        }
        if (request.getNumeroDocumentoPeluquero() == null || request.getNumeroDocumentoPeluquero().isBlank()) {
            throw new IllegalArgumentException("numeroDocumentoPeluquero es requerido");
        }
        if (request.getIdServicio() == null) {
            throw new IllegalArgumentException("idServicio es requerido");
        }
        validarFechaYHoras(request.getFechaCita(), request.getHoraInicioCita(), request.getHoraFinCita());
    }

    public void validarSolicitudAgendamiento(CitaCreateDTO request, String numeroDocumentoCliente) {
        if (request == null) {
            throw new IllegalArgumentException("Request es requerido");
        }
        if (numeroDocumentoCliente == null || numeroDocumentoCliente.isBlank()) {
            throw new IllegalArgumentException("numeroDocumentoCliente no encontrado en el token");
        }
        if (request.getNumeroDocumentoPeluquero() == null || request.getNumeroDocumentoPeluquero().isBlank()) {
            throw new IllegalArgumentException("numeroDocumentoPeluquero es requerido");
        }
        if (request.getIdServicio() == null) {
            throw new IllegalArgumentException("idServicio es requerido");
        }
        validarFechaYHoras(request.getFechaCita(), request.getHoraInicioCita(), request.getHoraFinCita());
    }

    public void validarSolicitudReprogramacion(CitaReprogramarDTO request, String numeroDocumentoCliente) {
        if (request == null) {
            throw new IllegalArgumentException("Request es requerido");
        }
        if (numeroDocumentoCliente == null || numeroDocumentoCliente.isBlank()) {
            throw new IllegalArgumentException("numeroDocumentoCliente no encontrado en el token");
        }
        validarFechaYHoras(request.getFechaCita(), request.getHoraInicioCita(), request.getHoraFinCita());
    }

    public void validarFechaYHoras(LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin) {
        if (fechaCita == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("fechaCita, horaInicioCita y horaFinCita son requeridos");
        }
    }

    public void validarNoCita(Long noCita) {
        if (noCita == null) {
            throw new IllegalArgumentException("noCita es requerido");
        }
    }

    public Usuario obtenerClienteActivo(String numeroDocumentoCliente) {
        Usuario cliente = usuarioRepository.findByNumeroDocumento(numeroDocumentoCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con documento: " + numeroDocumentoCliente));
        if (cliente.getIdEstado() != null && cliente.getIdEstado().equals(4)) {
            throw new IllegalArgumentException("Tu cuenta está bloqueada y no puedes agendar citas");
        }
        return cliente;
    }

    public Usuario obtenerPeluqueroActivo(String numeroDocumentoPeluquero) {
        Usuario peluquero = usuarioRepository.findByNumeroDocumento(numeroDocumentoPeluquero)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado con documento: " + numeroDocumentoPeluquero));
        if (!peluquero.getIdRol().equals(2)) {
            throw new IllegalArgumentException("No es barbero");
        }
        if (peluquero.getIdEstado() != null && peluquero.getIdEstado().equals(5)) {
            throw new IllegalArgumentException("No se puede, el barbero esta deshabilitado");
        }
        return peluquero;
    }

    public void validarPermisoGestionCita(Cita cita, String numeroDocumentoSolicitante, String mensajeError) {
        if (numeroDocumentoSolicitante == null || numeroDocumentoSolicitante.isBlank()) {
            throw new IllegalArgumentException("numeroDocumento solicitante no encontrado en el token");
        }

        boolean esCliente = numeroDocumentoSolicitante.equals(cita.getNumeroDocumentoCliente());
        boolean esPeluquero = numeroDocumentoSolicitante.equals(cita.getNumeroDocumentoPeluquero());
        if (!esCliente && !esPeluquero) {
            throw new IllegalArgumentException(mensajeError);
        }
    }
}
