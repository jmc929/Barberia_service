package com.barberia.modules.modulo_agendamiento.models.entities;

import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity(name = "CitaAgendamiento")
@Table(name = "tbl_citas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noCita;

    private String numeroDocumentoCliente;
    private String numeroDocumentoPeluquero;
    private Long idServicio;
    private LocalDate fechaCita;
    private LocalTime horaInicioCita;
    private LocalTime horaFinCita;
    private Long idEstado;
    private Boolean citaConfirmada;
    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;
    private Instant fechaCreacion;
}
