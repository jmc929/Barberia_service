package com.barberia.modules.modulo_horarios.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para asociar un servicio a un peluquero.
 */
public class ActualizarEspecialidadesPeluqueroDTO {
    @NotNull
    private Long idServicio;

    public Long getIdServicio() { return idServicio; }
    public void setIdServicio(Long idServicio) { this.idServicio = idServicio; }
}
