package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.models.dtos.CitaCreateDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadRequestDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaDisponibilidadResponseDTO;
import com.barberia.modules.modulo_agendamiento.models.dtos.CitaReprogramarDTO;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_agendamiento.repositories.CitaRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.dao.CannotSerializeTransactionException; // NOSONAR - deprecated but still functional
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
    private static final String CITA_NO_ENCONTRADA = "Cita no encontrada para noCita: ";

    private final CitaRepository citaRepository;
    private final CitaAgendamientoValidator validator;

    public CitaAgendamientoService(CitaRepository citaRepository, CitaAgendamientoValidator validator) {
        this.citaRepository = citaRepository;
        this.validator = validator;
    }

    public CitaDisponibilidadResponseDTO consultarDisponibilidad(CitaDisponibilidadRequestDTO request) {
        validator.validarSolicitudDisponibilidad(request);

        LocalTime horaFin = request.getHoraFinCita();
        validator.validarHorarioNegocio(request.getFechaCita(), request.getHoraInicioCita(), horaFin);

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
            } catch (CannotSerializeTransactionException ex) { // NOSONAR - deprecated but still functional
                if (intento == reintentos) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("No se pudo agendar la cita por concurrencia");
    }

    private CitaDTO agendarImpl(CitaCreateDTO request, String numeroDocumentoCliente) {
        validator.validarSolicitudAgendamiento(request, numeroDocumentoCliente);

        validator.obtenerClienteActivo(numeroDocumentoCliente);
        validator.obtenerPeluqueroActivo(request.getNumeroDocumentoPeluquero());

        LocalTime horaFin = request.getHoraFinCita();
        validator.validarHorarioNegocio(request.getFechaCita(), request.getHoraInicioCita(), horaFin);

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
        validator.validarNoCita(noCita);
        if (motivoCancelacion == null || motivoCancelacion.isBlank()) {
            throw new IllegalArgumentException("motivoCancelacion es requerido");
        }

        Cita cita = citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException(CITA_NO_ENCONTRADA + noCita));

        validator.validarPermisoGestionCita(cita, numeroDocumentoSolicitante, "No tienes permiso para cancelar esta cita");

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
            } catch (CannotSerializeTransactionException ex) { // NOSONAR - deprecated but still functional
                if (intento == reintentos) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("No se pudo reprogramar la cita por concurrencia");
    }

    private CitaDTO reprogramarImpl(Long noCita, CitaReprogramarDTO request, String numeroDocumentoCliente) {
        validator.validarNoCita(noCita);
        validator.validarSolicitudReprogramacion(request, numeroDocumentoCliente);

        Cita cita = citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException(CITA_NO_ENCONTRADA + noCita));

        validator.validarPermisoGestionCita(cita, numeroDocumentoCliente, "No puedes reprogramar una cita que no te pertenece");

        if (cita.getIdEstado() == null || cita.getIdEstado() != ESTADO_ACTIVO) {
            throw new IllegalArgumentException("La cita no está activa o ya fue gestionada");
        }

        LocalTime horaFin = request.getHoraFinCita();
        validator.validarHorarioNegocio(request.getFechaCita(), request.getHoraInicioCita(), horaFin);

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
        validator.validarNoCita(noCita);

        Cita cita = citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException(CITA_NO_ENCONTRADA + noCita));

        if (!numeroDocumentoPeluquero.equals(cita.getNumeroDocumentoPeluquero())) {
            throw new IllegalArgumentException("No puedes confirmar una cita que no te pertenece");
        }

        if (cita.getIdEstado() == null || cita.getIdEstado() != ESTADO_ACTIVO) {
            throw new IllegalArgumentException("La cita no está activa");
        }

        cita.setCitaConfirmada(true);
        return convertirADTO(citaRepository.save(cita));
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
