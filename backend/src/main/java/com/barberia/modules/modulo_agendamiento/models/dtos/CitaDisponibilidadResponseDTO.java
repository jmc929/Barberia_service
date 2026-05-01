package com.barberia.modules.modulo_agendamiento.models.dtos;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaDisponibilidadResponseDTO {
    private boolean disponible;
    private String mensaje;
    private String numeroDocumentoPeluquero;
    private Long idServicio;
    private LocalDate fechaCita;
    private LocalTime horaInicioCita;
    private LocalTime horaFinCita;
}
