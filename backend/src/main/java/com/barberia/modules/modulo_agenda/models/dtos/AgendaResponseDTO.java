package com.barberia.modules.modulo_agenda.models.dtos;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de respuesta para la agenda de citas de peluquero.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendaResponseDTO {
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String nombreCliente;
    private String nombreServicio;
}
