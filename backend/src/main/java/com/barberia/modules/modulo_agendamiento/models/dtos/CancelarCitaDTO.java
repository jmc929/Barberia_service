package com.barberia.modules.modulo_agendamiento.models.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelarCitaDTO {

    @NotBlank
    private String motivoCancelacion;
}