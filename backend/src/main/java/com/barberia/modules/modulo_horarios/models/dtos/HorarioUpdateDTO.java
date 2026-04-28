package com.barberia.modules.modulo_horarios.models.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioUpdateDTO {
    private String horaApertura;
    private String horaCierre;
}
