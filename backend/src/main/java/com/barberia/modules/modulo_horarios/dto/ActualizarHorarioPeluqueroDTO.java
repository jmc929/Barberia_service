package com.barberia.modules.modulo_horarios.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO para la actualización del horario laboral del peluquero.
 */
public class ActualizarHorarioPeluqueroDTO {
    @NotNull
    @Size(min = 1, max = 7)
    private List<DiaHorario> dias;

    public List<DiaHorario> getDias() { return dias; }
    public void setDias(List<DiaHorario> dias) { this.dias = dias; }

    /**
     * Clase interna para representar el horario de un día específico.
     */
    public static class DiaHorario {
        @NotNull
        private DayOfWeek diaSemana;
        @NotNull
        private LocalTime horaInicio;
        @NotNull
        private LocalTime horaFin;

        public DayOfWeek getDiaSemana() { return diaSemana; }
        public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }
        public LocalTime getHoraInicio() { return horaInicio; }
        public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    }
}
