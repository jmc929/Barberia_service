package com.barberia.modules.modulo_citas.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad que define los horarios de trabajo de cada barbero por día de la semana
 * Permite que cada barbero tenga horarios personalizados (override de HorarioNegocio)
 */
@Entity
@Table(
    name = "tbl_barbero_horario",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_usuario_barbero", "id_dia_semana"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberoHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_barbero_horario")
    private Long idBarberoHorario;

    @Column(name = "id_usuario_barbero", nullable = false)
    private String idUsuarioBarbero;

    @Column(name = "id_dia_semana", nullable = false)
    private Integer idDiaSemana; // 1=Lunes, 2=Martes, ..., 7=Domingo

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "disponible", nullable = false)
    private Boolean disponible = true; // false para descansos puntuales

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
