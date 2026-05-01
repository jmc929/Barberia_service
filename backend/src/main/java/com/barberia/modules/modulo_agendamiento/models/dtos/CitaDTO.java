package com.barberia.modules.modulo_agendamiento.models.dtos;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaDTO {
    private Long noCita;
    private String numeroDocumentoCliente;
    private String numeroDocumentoPeluquero;
    private Long idServicio;
    private LocalDate fechaCita;
    private LocalTime horaInicioCita;
    private LocalTime horaFinCita;
    private Long idEstado;
    private Boolean citaConfirmada;
    private Instant fechaCreacion;
}
