package com.barberia.modules.modulo_horarios.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioNegocioDTO {
    private Long idHorarioNegocio;
    private Long idDia;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaApertura;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaCierre;
    private Boolean localAbierto;
}
