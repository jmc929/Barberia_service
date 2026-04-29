package com.barberia.modules.modulo_citas.services;

import com.barberia.modules.modulo_citas.models.entities.BarberoHorario;
import com.barberia.modules.modulo_citas.models.entities.Cita;
import com.barberia.modules.modulo_horarios.models.entities.HorarioNegocio;
import com.barberia.modules.modulo_citas.models.dtos.DisponibilidadDTO;
import com.barberia.modules.modulo_citas.repositories.BarberoHorarioRepository;
import com.barberia.modules.modulo_citas.repositories.CitaRepository;
import com.barberia.modules.modulo_horarios.repositories.HorarioNegocioRepository;
import com.barberia.modules.modulo_servicios.models.entities.Servicio;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para calcular disponibilidad de barberos
 * Combina horarios globales + individuales y resta citas confirmadas
 */
@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private final BarberoHorarioRepository barberoHorarioRepository;
    private final HorarioNegocioRepository horarioNegocioRepository;
    private final CitaRepository citaRepository;

    /**
     * Obtiene slots disponibles de un barbero para una fecha y servicio específico
     *
     * @param idBarbero ID del barbero
     * @param nombreBarbero Nombre del barbero (para respuesta)
     * @param fecha Fecha a consultar (YYYY-MM-DD)
     * @param servicio Servicio (para obtener duración)
     * @return DTO con slots libres y ocupados
     */
    @Transactional(readOnly = true)
    public DisponibilidadDTO obtenerDisponibilidad(String idBarbero, String nombreBarbero,
                                                    LocalDate fecha, Servicio servicio) {

        // 1. Obtener horarios (barbero + negocio)
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);

        Long diaSemana = (long) convertirDiaASemana(fecha.getDayOfWeek());

        // Horario del barbero para ese día (si existe)
        Optional<BarberoHorario> barberoHorarioOpt = barberoHorarioRepository
                .findByIdUsuarioBarberoAndIdDiaSemana(idBarbero, diaSemana.intValue());

        // Si barbero tiene descanso puntual
        if (barberoHorarioOpt.isPresent() && !barberoHorarioOpt.get().getDisponible()) {
            return DisponibilidadDTO.builder()
                    .idBarbero(idBarbero)
                    .nombreBarbero(nombreBarbero)
                    .fecha(fecha.toString())
                    .horaInicio(null)
                    .horaFin(null)
                    .duracionServicioMinutos(servicio.getDuracion())
                    .slotsLibres(new ArrayList<>())
                    .ocupados(new ArrayList<>())
                    .build();
        }

        // Obtener horarios efectivos
        LocalTime horaInicio;
        LocalTime horaFin;

        if (barberoHorarioOpt.isPresent()) {
            // Usar horario del barbero (override)
            horaInicio = barberoHorarioOpt.get().getHoraInicio();
            horaFin = barberoHorarioOpt.get().getHoraFin();
        } else {
            // Usar horario del negocio
            Optional<HorarioNegocio> horarioNegocio = horarioNegocioRepository
                    .findByIdDia(diaSemana);

            if (horarioNegocio.isEmpty() || !horarioNegocio.get().getLocalAbierto()) {
                // Negocio cerrado ese día
                return DisponibilidadDTO.builder()
                        .idBarbero(idBarbero)
                        .nombreBarbero(nombreBarbero)
                        .fecha(fecha.toString())
                        .horaInicio(null)
                        .horaFin(null)
                        .duracionServicioMinutos(servicio.getDuracion())
                        .slotsLibres(new ArrayList<>())
                        .ocupados(new ArrayList<>())
                        .build();
            }

            horaInicio = horarioNegocio.get().getHoraApertura();
            horaFin = horarioNegocio.get().getHoraCierre();
        }

        // 2. Obtener citas confirmadas del barbero ese día
        List<Cita> citasConfirmadas = citaRepository
                .findByIdBarberoAndFechaHoraBetweenAndEstadoCita(
                    idBarbero,
                    inicioDelDia,
                    finDelDia,
                    EstadoCita.CONFIRMADA
                );

        // 3. Generar slots de 30 minutos
        List<String> slotsLibres = new ArrayList<>();
        List<String> ocupados = new ArrayList<>();

        LocalTime actual = horaInicio;
        int duracionServicio = servicio.getDuracion();

        while (actual.plusMinutes(duracionServicio).compareTo(horaFin) <= 0) {
            String slot = actual.toString();
            LocalDateTime slotDateTime = fecha.atTime(actual);
            LocalDateTime slotFin = slotDateTime.plusMinutes(duracionServicio);

            // Verificar si hay solapamiento con citas existentes
            boolean estaOcupado = citasConfirmadas.stream()
                    .anyMatch(cita -> {
                        LocalDateTime citaFin = cita.getFechaHora().plusMinutes(cita.getDuracionMinutos());
                        return slotDateTime.isBefore(citaFin) && slotFin.isAfter(cita.getFechaHora());
                    });

            if (estaOcupado) {
                ocupados.add(slot);
            } else {
                slotsLibres.add(slot);
            }

            actual = actual.plusMinutes(30); // Incrementar cada 30 minutos
        }

        return DisponibilidadDTO.builder()
                .idBarbero(idBarbero)
                .nombreBarbero(nombreBarbero)
                .fecha(fecha.toString())
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .duracionServicioMinutos(duracionServicio)
                .slotsLibres(slotsLibres)
                .ocupados(ocupados)
                .build();
    }

    /**
     * Valida si un slot está disponible (sin solapamientos)
     *
     * @return true si está disponible, false si está ocupado
     */
    @Transactional(readOnly = true)
    public boolean validarDisponibilidad(String idBarbero, LocalDateTime fechaHora, Integer duracionMinutos) {
        LocalDateTime fin = fechaHora.plusMinutes(duracionMinutos);

        List<Cita> citasConflictivas = citaRepository
                .findByIdBarberoAndFechaHoraBetweenAndEstadoCita(
                    idBarbero,
                    fechaHora.minusMinutes(1),
                    fin.plusMinutes(1),
                    EstadoCita.CONFIRMADA
                );

        // Verificar si hay solapamiento
        return citasConflictivas.stream()
                .noneMatch(cita -> {
                    LocalDateTime citaFin = cita.getFechaHora().plusMinutes(cita.getDuracionMinutos());
                    return fechaHora.isBefore(citaFin) && fin.isAfter(cita.getFechaHora());
                });
    }

    /**
     * Convierte DayOfWeek de Java a ID de día (1=Lunes, 7=Domingo)
     */
    private int convertirDiaASemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> 1;
            case TUESDAY -> 2;
            case WEDNESDAY -> 3;
            case THURSDAY -> 4;
            case FRIDAY -> 5;
            case SATURDAY -> 6;
            case SUNDAY -> 7;
        };
    }
}
