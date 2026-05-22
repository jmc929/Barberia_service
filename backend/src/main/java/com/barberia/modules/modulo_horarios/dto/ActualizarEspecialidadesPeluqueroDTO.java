package com.barberia.modules.modulo_horarios.dto;

import jakarta.validation.constraints.NotNull;

public class ActualizarEspecialidadesPeluqueroDTO {
    @NotNull
    private Long idServicio;

    public Long getIdServicio() { return idServicio; }
    public void setIdServicio(Long idServicio) { this.idServicio = idServicio; }
}
