package com.barberia.modules.modulo_agendamiento.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaReprogramarDTO {

    @NotNull
    private LocalDate fechaCita;

    @NotNull
    private LocalTime horaInicioCita;
}
