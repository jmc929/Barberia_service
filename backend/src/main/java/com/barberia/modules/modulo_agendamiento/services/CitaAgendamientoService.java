package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.models.dtos.CitaCreateDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadRequestDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadResponseDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaReprogramarDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CancelarCitaDTO;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_agendamiento.repositories.CitaRepository;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.modules.modulo_usuarios.models.entities.Usuario;
import com.barberia.modules.modulo_usuarios.repositories.UsuarioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CitaAgendamientoService {

    private static final long ESTADO_ACTIVO = 1L;
    private static final long ESTADO_CANCELADA = 3L;

    private final CitaRepository citaRepository;
    private final HorarioNegocioRepository horarioNegocioRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaAgendamientoService(CitaRepository citaRepository, HorarioNegocioRepository horarioNegocioRepository, UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.horarioNegocioRepository = horarioNegocioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private long calcularIdDia(LocalDate fechaCita) {
        return fechaCita.getDayOfWeek().getValue();
    }

    private HorarioNegocioDTO obtenerHorarioParaFecha(LocalDate fechaCita) {
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

    private void validarHorarioNegocio(LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin) {
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

    public CitaDisponibilidadResponseDTO consultarDisponibilidad(CitaDisponibilidadRequestDTO request) {
        validarSolicitudDisponibilidad(request);

        LocalTime horaFin = request.getHoraFinCita();
        validarHorarioNegocio(request.getFechaCita(), request.getHoraInicioCita(), horaFin);

        List<Cita> conflictos = buscarConflictos(
                request.getNumeroDocumentoPeluquero(),
                request.getFechaCita(),
                request.getHoraInicioCita(),
                horaFin
        );

        boolean disponible = conflictos.isEmpty();
        return CitaDisponibilidadResponseDTO.builder()
                .disponible(disponible)
                .mensaje(disponible ? "Disponible" : "No disponible: existe una cita solapada")
                .numeroDocumentoPeluquero(request.getNumeroDocumentoPeluquero())
                .idServicio(request.getIdServicio())
                .fechaCita(request.getFechaCita())
                .horaInicioCita(request.getHoraInicioCita())
                .horaFinCita(horaFin)
                .build();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CitaDTO agendar(CitaCreateDTO request, String numeroDocumentoCliente) {
        int reintentos = 1;
        for (int intento = 0; intento <= reintentos; intento++) {
            try {
                return agendarImpl(request, numeroDocumentoCliente);
            } catch (CannotSerializeTransactionException ex) {
                if (intento == reintentos) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("No se pudo agendar la cita por concurrencia");
    }

    private CitaDTO agendarImpl(CitaCreateDTO request, String numeroDocumentoCliente) {
        validarSolicitudAgendamiento(request, numeroDocumentoCliente);

        obtenerClienteActivo(numeroDocumentoCliente);
        obtenerPeluqueroActivo(request.getNumeroDocumentoPeluquero());

        LocalTime horaFin = request.getHoraFinCita();
        validarHorarioNegocio(request.getFechaCita(), request.getHoraInicioCita(), horaFin);

        List<Cita> conflictos = buscarConflictosParaAgendar(
                request.getNumeroDocumentoPeluquero(),
                request.getFechaCita(),
                request.getHoraInicioCita(),
                horaFin
        );

        if (!conflictos.isEmpty()) {
            throw new IllegalArgumentException("No se puede agendar: existe una cita solapada");
        }

        return convertirADTO(citaRepository.save(construirCita(request, numeroDocumentoCliente, horaFin)));
    }

    @Transactional
    public CitaDTO cancelar(Long noCita, String motivoCancelacion, String numeroDocumentoSolicitante) {
        validarNoCita(noCita);
        if (motivoCancelacion == null || motivoCancelacion.isBlank()) {
            throw new IllegalArgumentException("motivoCancelacion es requerido");
        }

        Cita cita = citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada para noCita: " + noCita));

        validarPermisoGestionCita(cita, numeroDocumentoSolicitante, "No tienes permiso para cancelar esta cita");

        if (cita.getIdEstado() == null || cita.getIdEstado() != ESTADO_ACTIVO) {
            throw new IllegalArgumentException("La cita no está activa o ya fue gestionada");
        }

        cita.setMotivoCancelacion(motivoCancelacion);
        cita.setIdEstado(ESTADO_CANCELADA);
        return convertirADTO(citaRepository.save(cita));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CitaDTO reprogramar(Long noCita, CitaReprogramarDTO request, String numeroDocumentoCliente) {
        int reintentos = 1;
        for (int intento = 0; intento <= reintentos; intento++) {
            try {
                return reprogramarImpl(noCita, request, numeroDocumentoCliente);
            } catch (CannotSerializeTransactionException ex) {
                if (intento == reintentos) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("No se pudo reprogramar la cita por concurrencia");
    }

    private CitaDTO reprogramarImpl(Long noCita, CitaReprogramarDTO request, String numeroDocumentoCliente) {
        validarNoCita(noCita);
        validarSolicitudReprogramacion(request, numeroDocumentoCliente);

        Cita cita = citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada para noCita: " + noCita));

        validarPermisoGestionCita(cita, numeroDocumentoCliente, "No puedes reprogramar una cita que no te pertenece");

        if (cita.getIdEstado() == null || cita.getIdEstado() != ESTADO_ACTIVO) {
            throw new IllegalArgumentException("La cita no está activa o ya fue gestionada");
        }

        LocalTime horaFin = request.getHoraFinCita();
        validarHorarioNegocio(request.getFechaCita(), request.getHoraInicioCita(), horaFin);

        List<Cita> conflictos = buscarConflictosExcluyendoCita(
                cita.getNumeroDocumentoPeluquero(),
                cita.getNoCita(),
                request.getFechaCita(),
                request.getHoraInicioCita(),
                horaFin
        );

        if (!conflictos.isEmpty()) {
            throw new IllegalArgumentException("No se puede reprogramar: existe una cita solapada");
        }

        cita.setFechaCita(request.getFechaCita());
        cita.setHoraInicioCita(request.getHoraInicioCita());
        cita.setHoraFinCita(horaFin);
        cita.setIdEstado(ESTADO_ACTIVO);

        return convertirADTO(citaRepository.save(cita));
    }

    @Transactional
    public CitaDTO confirmar(Long noCita, String numeroDocumentoPeluquero) {
        validarNoCita(noCita);

        Cita cita = citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada para noCita: " + noCita));

        if (!numeroDocumentoPeluquero.equals(cita.getNumeroDocumentoPeluquero())) {
            throw new IllegalArgumentException("No puedes confirmar una cita que no te pertenece");
        }

        if (cita.getIdEstado() == null || cita.getIdEstado() != ESTADO_ACTIVO) {
            throw new IllegalArgumentException("La cita no está activa");
        }

        cita.setCitaConfirmada(true);
        return convertirADTO(citaRepository.save(cita));
    }

    private void validarSolicitudDisponibilidad(CitaDisponibilidadRequestDTO request) {
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

    private void validarSolicitudAgendamiento(CitaCreateDTO request, String numeroDocumentoCliente) {
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

    private void validarSolicitudReprogramacion(CitaReprogramarDTO request, String numeroDocumentoCliente) {
        if (request == null) {
            throw new IllegalArgumentException("Request es requerido");
        }
        if (numeroDocumentoCliente == null || numeroDocumentoCliente.isBlank()) {
            throw new IllegalArgumentException("numeroDocumentoCliente no encontrado en el token");
        }
        validarFechaYHoras(request.getFechaCita(), request.getHoraInicioCita(), request.getHoraFinCita());
    }

    private void validarFechaYHoras(LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin) {
        if (fechaCita == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("fechaCita, horaInicioCita y horaFinCita son requeridos");
        }
    }

    private void validarNoCita(Long noCita) {
        if (noCita == null) {
            throw new IllegalArgumentException("noCita es requerido");
        }
    }

    private Usuario obtenerClienteActivo(String numeroDocumentoCliente) {
        Usuario cliente = usuarioRepository.findByNumeroDocumento(numeroDocumentoCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con documento: " + numeroDocumentoCliente));
        if (cliente.getIdEstado() != null && cliente.getIdEstado().equals(4)) {
            throw new IllegalArgumentException("Tu cuenta está bloqueada y no puedes agendar citas");
        }
        return cliente;
    }

    private Usuario obtenerPeluqueroActivo(String numeroDocumentoPeluquero) {
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

    private void validarPermisoGestionCita(Cita cita, String numeroDocumentoSolicitante, String mensajeError) {
        if (numeroDocumentoSolicitante == null || numeroDocumentoSolicitante.isBlank()) {
            throw new IllegalArgumentException("numeroDocumento solicitante no encontrado en el token");
        }

        boolean esCliente = numeroDocumentoSolicitante.equals(cita.getNumeroDocumentoCliente());
        boolean esPeluquero = numeroDocumentoSolicitante.equals(cita.getNumeroDocumentoPeluquero());
        if (!esCliente && !esPeluquero) {
            throw new IllegalArgumentException(mensajeError);
        }
    }

    private List<Cita> buscarConflictos(String numeroDocumentoPeluquero, LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin) {
        return citaRepository.findConflicts(
                numeroDocumentoPeluquero,
                fechaCita,
                ESTADO_ACTIVO,
                horaInicio,
                horaFin
        );
    }

    private List<Cita> buscarConflictosParaAgendar(String numeroDocumentoPeluquero, LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin) {
        return citaRepository.findConflictsForUpdate(
                numeroDocumentoPeluquero,
                fechaCita,
                ESTADO_ACTIVO,
                horaInicio,
                horaFin
        );
    }

    private List<Cita> buscarConflictosExcluyendoCita(String numeroDocumentoPeluquero, Long noCita, LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin) {
        return citaRepository.findConflictsForUpdateExcludingNoCita(
                numeroDocumentoPeluquero,
                noCita,
                fechaCita,
                ESTADO_ACTIVO,
                horaInicio,
                horaFin
        );
    }

    private Cita construirCita(CitaCreateDTO request, String numeroDocumentoCliente, LocalTime horaFin) {
        Instant now = Instant.now();
        return Cita.builder()
                .numeroDocumentoCliente(numeroDocumentoCliente)
                .numeroDocumentoPeluquero(request.getNumeroDocumentoPeluquero())
                .idServicio(request.getIdServicio())
                .fechaCita(request.getFechaCita())
                .horaInicioCita(request.getHoraInicioCita())
                .horaFinCita(horaFin)
                .idEstado(ESTADO_ACTIVO)
                .citaConfirmada(false)
                .fechaCreacion(now)
                .build();
    }

    private CitaDTO convertirADTO(Cita c) {
        return CitaDTO.builder()
                .noCita(c.getNoCita())
                .numeroDocumentoCliente(c.getNumeroDocumentoCliente())
                .numeroDocumentoPeluquero(c.getNumeroDocumentoPeluquero())
                .idServicio(c.getIdServicio())
                .fechaCita(c.getFechaCita())
                .horaInicioCita(c.getHoraInicioCita())
                .horaFinCita(c.getHoraFinCita())
                .idEstado(c.getIdEstado())
                .citaConfirmada(c.getCitaConfirmada())
                .motivoCancelacion(c.getMotivoCancelacion())
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }
}
