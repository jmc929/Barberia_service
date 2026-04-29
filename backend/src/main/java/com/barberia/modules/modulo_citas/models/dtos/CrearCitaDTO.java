package com.barberia.modules.modulo_citas.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para crear una nueva cita
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearCitaDTO {

    @NotNull(message = "El ID del barbero es obligatorio")
    private String idBarbero;

    @NotNull(message = "El ID del servicio es obligatorio")
    @Positive(message = "El ID del servicio debe ser positivo")
    private Long idServicio;

    @NotNull(message = "La fecha y hora son obligatorias")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;
}
