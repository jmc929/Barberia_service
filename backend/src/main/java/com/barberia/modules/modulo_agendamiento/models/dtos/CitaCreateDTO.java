package com.barberia.modules.modulo_agendamiento.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaCreateDTO {

    @NotBlank
    private String numeroDocumentoCliente;

    @NotBlank
    private String numeroDocumentoPeluquero;

    @NotNull
    private Long idServicio;

    @NotNull
    private LocalDate fechaCita;

    @NotNull
    private LocalTime horaInicioCita;

    private String meta;
}
