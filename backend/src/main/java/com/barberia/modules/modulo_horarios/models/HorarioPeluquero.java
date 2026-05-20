package com.barberia.modules.modulo_horarios.models;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Entidad que representa el horario laboral de un peluquero para un día específico.
 */
@Entity
@Table(name = "tbl_horarios_peluqueros")
public class HorarioPeluquero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horarios_peluqueros")
    private Long idHorariosPeluqueros;

    @Column(name = "numero_documento_peluquero", nullable = false)
    private String numeroDocumentoPeluquero;

    @Column(name = "id_dia", nullable = false)
    private Long idDia;

    @Column(name = "hora_inicio_horario", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin_horario", nullable = false)
    private LocalTime horaFin;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // Getters y setters
    public Long getIdHorariosPeluqueros() { return idHorariosPeluqueros; }
    public void setIdHorariosPeluqueros(Long idHorariosPeluqueros) { this.idHorariosPeluqueros = idHorariosPeluqueros; }

    public String getNumeroDocumentoPeluquero() { return numeroDocumentoPeluquero; }
    public void setNumeroDocumentoPeluquero(String numeroDocumentoPeluquero) { this.numeroDocumentoPeluquero = numeroDocumentoPeluquero; }

    public Long getIdDia() { return idDia; }
    public void setIdDia(Long idDia) { this.idDia = idDia; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
