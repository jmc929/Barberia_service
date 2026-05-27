package com.barberia.modules.modulo_agenda.models.dtos;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendaResponseDTO {
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Long noCita;
    private String nombreCliente;
    private Long idServicio;
    private String nombreServicio;
    private String estado;
}
