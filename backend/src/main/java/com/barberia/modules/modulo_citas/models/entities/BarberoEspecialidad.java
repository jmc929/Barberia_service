package com.barberia.modules.modulo_citas.models.entities;

import com.barberia.modules.modulo_citas.enums.NivelEspecialidad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que relaciona un barbero con un servicio que ofrece
 * Permite definir qué servicios ofrece cada barbero y su nivel de experiencia
 */
@Entity
@Table(
    name = "tbl_barbero_especialidad",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_usuario_barbero", "id_servicio"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberoEspecialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_barbero_especialidad")
    private Long idBarberoEspecialidad;

    @Column(name = "id_usuario_barbero", nullable = false)
    private String idUsuarioBarbero;

    @Column(name = "id_servicio", nullable = false)
    private Long idServicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_especialidad", nullable = false)
    private NivelEspecialidad nivelEspecialidad;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
