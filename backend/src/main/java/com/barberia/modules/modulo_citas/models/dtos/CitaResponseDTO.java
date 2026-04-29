package com.barberia.modules.modulo_citas.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.barberia.modules.modulo_citas.enums.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para responder información de una cita
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaResponseDTO {

    private Long idCita;

    private String cliente;

    private String clienteEmail;

    private String barbero;

    private String servicio;

    private Integer duracionMinutos;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;

    private BigDecimal costo;

    private String estado;

    private String notas;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
