package com.barberia.modules.modulo_citas.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalTime;

/**
 * DTO que retorna disponibilidad de un barbero para un día específico
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadDTO {

    private String idBarbero;

    private String nombreBarbero;

    private String fecha; // YYYY-MM-DD

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    private Integer duracionServicioMinutos;

    private List<String> slotsLibres; // ["08:00", "08:30", "09:00", ...]

    private List<String> ocupados; // ["10:00", "10:30", "14:30"]
}
