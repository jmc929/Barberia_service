package com.barberia.modules.modulo_horarios.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "tbl_horarios_negocio")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario_negocio")
    private Long idHorarioNegocio;

    @Column(name = "id_dia", nullable = false)
    private Long idDia;

    @Column(name = "hora_apertura")
    private LocalTime horaApertura;

    @Column(name = "hora_cierre")
    private LocalTime horaCierre;

    @Column(name = "local_abierto", nullable = false)
    @Builder.Default
    private Boolean localAbierto = true;
}
