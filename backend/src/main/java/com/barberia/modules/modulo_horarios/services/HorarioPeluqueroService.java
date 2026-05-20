package com.barberia.modules.modulo_horarios.services;

import com.barberia.modules.modulo_horarios.dto.ActualizarHorarioPeluqueroDTO;
import com.barberia.modules.modulo_horarios.exceptions.HorarioInvalidoException;
import com.barberia.modules.modulo_horarios.models.HorarioPeluquero;
import com.barberia.modules.modulo_horarios.repositories.HorarioPeluqueroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        // Validación: no permitir horas inválidas ni solapamientos
        validarHorario(dto);
        // Eliminar horarios anteriores
        horarioPeluqueroRepository.findByNumeroDocumentoPeluquero(numeroDocumentoPeluquero)
                .forEach(horarioPeluqueroRepository::delete);
        // Guardar nuevos horarios
        LocalDateTime ahora = LocalDateTime.now();
        List<HorarioPeluquero> nuevos = dto.getDias().stream().map(dia -> {
            HorarioPeluquero h = new HorarioPeluquero();
            h.setNumeroDocumentoPeluquero(numeroDocumentoPeluquero);
            h.setIdDia((long) dia.getDiaSemana().getValue());
            h.setHoraInicio(dia.getHoraInicio());
            h.setHoraFin(dia.getHoraFin());
            h.setFechaCreacion(ahora);
            h.setFechaActualizacion(ahora);
            return h;
        }).collect(Collectors.toList());
        horarioPeluqueroRepository.saveAll(nuevos);
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
        for (ActualizarHorarioPeluqueroDTO.DiaHorario dia : dto.getDias()) {
            if (dia.getHoraInicio().isAfter(dia.getHoraFin()) || dia.getHoraInicio().equals(dia.getHoraFin())) {
                throw new HorarioInvalidoException("La hora de inicio debe ser menor que la de fin para el día " + dia.getDiaSemana());
            }
            // Aquí podrías validar contra el horario del local si tienes acceso a esa info
        }
        // Validar solapamientos
        List<ActualizarHorarioPeluqueroDTO.DiaHorario> dias = dto.getDias();
        for (int i = 0; i < dias.size(); i++) {
            for (int j = i + 1; j < dias.size(); j++) {
                if (dias.get(i).getDiaSemana() == dias.get(j).getDiaSemana()) {
                    throw new HorarioInvalidoException("No puede haber más de un rango horario por día");
                }
            }
        }
    }
}
