package com.barberia.modules.modulo_agendamiento.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaDisponibilidadRequestDTO {

    @NotBlank
    private String numeroDocumentoPeluquero;

    @NotNull
    private Long idServicio;

    @NotNull
    @Schema(type = "string", example = "2026-04-18")
    private LocalDate fechaCita;

    @NotNull
    @Schema(type = "string", example = "07:00:00")
    private LocalTime horaInicioCita;

    @NotNull
    @Schema(type = "string", example = "07:30:00")
    private LocalTime horaFinCita;
}
