package com.barberia.modules.modulo_agendamiento.services;

import com.barberia.modules.modulo_agendamiento.models.dtos.*;
import com.barberia.modules.modulo_agendamiento.models.entities.Cita;
import com.barberia.modules.modulo_agendamiento.repositories.CitaRepository;
import com.barberia.modules.modulo_horarios.models.dtos.HorarioNegocioDTO;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.shared.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final long ESTADO_INACTIVO = 2L;
    private static final long ESTADO_CANCELADA = 3L;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioNegocioRepository horarioNegocioRepository;

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

    private void validarHorario(LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin) {
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
        if (request == null) {
            throw new IllegalArgumentException("Request es requerido");
        }
        if (request.getNumeroDocumentoPeluquero() == null || request.getNumeroDocumentoPeluquero().isBlank()) {
            throw new IllegalArgumentException("numeroDocumentoPeluquero es requerido");
        }
        if (request.getIdServicio() == null) {
            throw new IllegalArgumentException("idServicio es requerido");
        }
        if (request.getFechaCita() == null || request.getHoraInicioCita() == null || request.getHoraFinCita() == null) {
            throw new IllegalArgumentException("fechaCita, horaInicioCita y horaFinCita son requeridos");
        }

        LocalTime horaFin = request.getHoraFinCita();
        validarHorario(request.getFechaCita(), request.getHoraInicioCita(), horaFin);

        List<Cita> conflictos = citaRepository.findConflicts(
                request.getNumeroDocumentoPeluquero(),
                request.getFechaCita(),
                ESTADO_ACTIVO,
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
        if (request.getFechaCita() == null || request.getHoraInicioCita() == null || request.getHoraFinCita() == null) {
            throw new IllegalArgumentException("fechaCita, horaInicioCita y horaFinCita son requeridos");
        }

        LocalTime horaFin = request.getHoraFinCita();
        validarHorario(request.getFechaCita(), request.getHoraInicioCita(), horaFin);

        List<Cita> conflictos = citaRepository.findConflictsForUpdate(
                request.getNumeroDocumentoPeluquero(),
                request.getFechaCita(),
                ESTADO_ACTIVO,
                request.getHoraInicioCita(),
                horaFin
        );

        if (!conflictos.isEmpty()) {
            throw new IllegalArgumentException("No se puede agendar: existe una cita solapada");
        }

        Instant now = Instant.now();
        Cita cita = Cita.builder()
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

        Cita guardada = citaRepository.save(cita);
        return convertirADTO(guardada);
    }

    @Transactional
    public CitaDTO cancelar(Long noCita) {
        if (noCita == null) {
            throw new IllegalArgumentException("noCita es requerido");
        }

        Cita cita = citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada para noCita: " + noCita));

        if (cita.getIdEstado() == null || cita.getIdEstado() != ESTADO_ACTIVO) {
            throw new IllegalArgumentException("La cita no está activa o ya fue gestionada");
        }

        cita.setIdEstado(ESTADO_CANCELADA);
        Cita guardada = citaRepository.save(cita);
        return convertirADTO(guardada);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CitaDTO reprogramar(Long noCita, CitaReprogramarDTO request) {
        int reintentos = 1;
        for (int intento = 0; intento <= reintentos; intento++) {
            try {
                return reprogramarImpl(noCita, request);
            } catch (CannotSerializeTransactionException ex) {
                if (intento == reintentos) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("No se pudo reprogramar la cita por concurrencia");
    }

    private CitaDTO reprogramarImpl(Long noCita, CitaReprogramarDTO request) {
        if (noCita == null) {
            throw new IllegalArgumentException("noCita es requerido");
        }
        if (request == null || request.getFechaCita() == null || request.getHoraInicioCita() == null || request.getHoraFinCita() == null) {
            throw new IllegalArgumentException("fechaCita, horaInicioCita y horaFinCita son requeridos");
        }

        Cita cita = citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada para noCita: " + noCita));

        if (cita.getIdEstado() == null || cita.getIdEstado() != ESTADO_ACTIVO) {
            throw new IllegalArgumentException("La cita no está activa o ya fue gestionada");
        }

        LocalTime horaFin = request.getHoraFinCita();
        validarHorario(request.getFechaCita(), request.getHoraInicioCita(), horaFin);

        List<Cita> conflictos = citaRepository.findConflictsForUpdateExcludingNoCita(
                cita.getNumeroDocumentoPeluquero(),
                cita.getNoCita(),
                request.getFechaCita(),
                ESTADO_ACTIVO,
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

        Cita guardada = citaRepository.save(cita);
        return convertirADTO(guardada);
    }

    @Transactional
    public CitaDTO confirmar(Long noCita, String numeroDocumentoPeluquero) {
        if (noCita == null) {
            throw new IllegalArgumentException("noCita es requerido");
        }

        Cita cita = citaRepository.findById(noCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada para noCita: " + noCita));

        if (!numeroDocumentoPeluquero.equals(cita.getNumeroDocumentoPeluquero())) {
            throw new IllegalArgumentException("No puedes confirmar una cita que no te pertenece");
        }

        if (cita.getIdEstado() == null || cita.getIdEstado() != ESTADO_ACTIVO) {
            throw new IllegalArgumentException("La cita no está activa");
        }

        cita.setCitaConfirmada(true);
        Cita guardada = citaRepository.save(cita);
        return convertirADTO(guardada);
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
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }
}
