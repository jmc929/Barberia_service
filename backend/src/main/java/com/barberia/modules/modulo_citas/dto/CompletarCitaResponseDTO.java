package com.barberia.modules.modulo_citas.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

/**
 * DTO de respuesta al completar una cita.
 */
@Data
@Builder
public class CompletarCitaResponseDTO {
    private Long idCita;
    private String estadoAnterior;
    private String estadoActual;
    private Instant fechaCompletado;
    private String mensaje;
}
