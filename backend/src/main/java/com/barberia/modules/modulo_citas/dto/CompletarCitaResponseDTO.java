package com.barberia.modules.modulo_citas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompletarCitaResponseDTO {
    private Long idCita;
    private String estadoAnterior;
    private String estadoActual;
    private String mensaje;
}
