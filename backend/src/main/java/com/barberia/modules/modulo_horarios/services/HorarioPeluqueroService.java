package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.dto.ActualizarHorarioPeluqueroDTO;
import com.barberia.modules.modulo_horarios.exceptions.HorarioInvalidoException;
import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import com.barberia.modules.modulo_horarios.repositories.HorarioPeluqueroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio para la gestión del horario laboral del peluquero.
 */
@Service
public class HorarioPeluqueroService {
    @Autowired
    private HorarioPeluqueroRepository horarioPeluqueroRepository;

    /**
     * Actualiza el horario laboral del peluquero.
     * @param peluqueroId ID del peluquero
     * @param dto DTO con los nuevos horarios
     */
    @Transactional
    public void actualizarHorario(String numeroDocumentoPeluquero, ActualizarHorarioPeluqueroDTO dto) {
        validarHorario(dto);

        LocalDateTime ahora = LocalDateTime.now();
        HorarioPeluquero horario = horarioPeluqueroRepository
                .findByNumeroDocumentoPeluqueroAndIdDia(numeroDocumentoPeluquero, dto.getIdDia())
                .orElseGet(HorarioPeluquero::new);

        if (horario.getFechaCreacion() == null) {
            horario.setFechaCreacion(ahora);
        }

        horario.setNumeroDocumentoPeluquero(numeroDocumentoPeluquero);
        horario.setIdDia(dto.getIdDia());
        horario.setHoraInicio(LocalTime.parse(dto.getHoraInicioHorario()));
        horario.setHoraFin(LocalTime.parse(dto.getHoraFinHorario()));
        horario.setFechaActualizacion(ahora);

        horarioPeluqueroRepository.save(horario);
    }

    /**
     * Obtiene el horario laboral del peluquero.
     */
    public List<HorarioPeluquero> obtenerHorario(String numeroDocumentoPeluquero) {
        return horarioPeluqueroRepository.findByNumeroDocumentoPeluquero(numeroDocumentoPeluquero);
    }

    /**
     * Valida que los horarios sean correctos.
     */
    private void validarHorario(ActualizarHorarioPeluqueroDTO dto) {
        LocalTime horaInicio = LocalTime.parse(dto.getHoraInicioHorario());
        LocalTime horaFin = LocalTime.parse(dto.getHoraFinHorario());
        if (horaInicio.isAfter(horaFin) || horaInicio.equals(horaFin)) {
            throw new HorarioInvalidoException("La hora de inicio debe ser menor que la de fin");
        }
    }
}
