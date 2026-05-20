package com.barberia.modules.modulo_horarios.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO para la actualización de especialidades del peluquero.
 */
public class ActualizarEspecialidadesPeluqueroDTO {
    @NotNull
    @Size(min = 1)
    private List<Long> especialidadesIds;

    public List<Long> getEspecialidadesIds() { return especialidadesIds; }
    public void setEspecialidadesIds(List<Long> especialidadesIds) { this.especialidadesIds = especialidadesIds; }
}
