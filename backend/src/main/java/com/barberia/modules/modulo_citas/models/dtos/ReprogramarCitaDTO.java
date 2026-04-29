package com.barberia.modules.modulo_citas.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para reprogramar una cita existente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReprogramarCitaDTO {

    @NotNull(message = "El ID del barbero es obligatorio")
    private String idBarbero;

    @NotNull(message = "La nueva fecha y hora son obligatorias")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;
}
