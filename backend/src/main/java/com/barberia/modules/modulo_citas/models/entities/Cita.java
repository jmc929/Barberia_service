package com.barberia.modules.modulo_citas.models.entities;

import com.barberia.modules.modulo_citas.enums.EstadoCita;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad CORE: Representa una cita agendada entre un cliente y un barbero
 * Incluye Pessimistic Locking para evitar race conditions en la creación
 * 
 * CONSTRAINT CRÍTICO:
 * - UNIQUE(id_barbero, fecha_hora, id_estado_cita) cuando estado=CONFIRMADA
 * - Previene solapamientos de citas
 */
@Entity
@Table(
    name = "tbl_citas",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_cita_barbero_fecha_confirmada",
            columnNames = {"id_barbero", "fecha_hora", "id_estado_cita"})
    },
    indexes = {
        @Index(name = "idx_cita_barbero_slot", columnList = "id_barbero,fecha_hora,id_estado_cita"),
        @Index(name = "idx_cita_cliente", columnList = "id_cliente"),
        @Index(name = "idx_cita_fecha", columnList = "fecha_hora"),
        @Index(name = "idx_cita_estado", columnList = "id_estado_cita")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita;

    @Column(name = "id_cliente", nullable = false)
    private String idCliente;

    @Column(name = "id_barbero", nullable = false)
    private String idBarbero;

    @Column(name = "id_servicio", nullable = false)
    private Long idServicio;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "id_estado_cita", nullable = false)
    private EstadoCita estadoCita = EstadoCita.CONFIRMADA;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(name = "costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
