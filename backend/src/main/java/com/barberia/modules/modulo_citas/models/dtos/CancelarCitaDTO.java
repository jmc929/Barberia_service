package com.barberia.modules.modulo_citas.models.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cancelar una cita
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelarCitaDTO {

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;
}
