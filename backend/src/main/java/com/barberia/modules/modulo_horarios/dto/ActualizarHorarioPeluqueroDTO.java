package com.barberia.modules.modulo_horarios.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para actualizar un horario puntual del peluquero.
 */
public class ActualizarHorarioPeluqueroDTO {
    @NotNull
    private Long idDia;

    @NotNull
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "horaInicioHorario debe tener formato HH:mm:ss")
    private String horaInicioHorario;

    @NotNull
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "horaFinHorario debe tener formato HH:mm:ss")
    private String horaFinHorario;

    public Long getIdDia() { return idDia; }
    public void setIdDia(Long idDia) { this.idDia = idDia; }

    public String getHoraInicioHorario() { return horaInicioHorario; }
    public void setHoraInicioHorario(String horaInicioHorario) { this.horaInicioHorario = horaInicioHorario; }

    public String getHoraFinHorario() { return horaFinHorario; }
    public void setHoraFinHorario(String horaFinHorario) { this.horaFinHorario = horaFinHorario; }
}
